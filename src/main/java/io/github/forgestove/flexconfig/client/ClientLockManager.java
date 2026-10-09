package io.github.forgestove.flexconfig.client;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.network.*;
import io.github.forgestove.flexconfig.platform.Platform;
import io.github.forgestove.flexconfig.tree.*;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.awt.Point;
import java.io.StringReader;
import java.util.*;
/**
 * 追踪服务端锁定的配置项，以 (模组 ID, 配置文件名) 为维度。
 * <p>
 * 在服务端发送 {@link ConfigSyncPayload} 时更新。
 * 锁定前备份原始值，断开连接或解锁时恢复。
 * 锁定值以 TOML 格式传输，由 {@link ConfigSerializer} 处理类型转换。
 * </p>
 */
public final class ClientLockManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	/** 待发送的锁定操作（延迟至保存时执行）。 */
	private static final Map<LockKey, Boolean> pendingLocks = new LinkedHashMap<>();
	/** 文件 → (配置路径 → 服务端确认的锁定值字符串)。 */
	private static final Map<FileKey, Map<String, String>> lockedConfigs = new HashMap<>();
	/** 锁定前的原值，解锁或断开时恢复。 */
	private static final Map<LockKey, Object> originalValues = new HashMap<>();
	/** 服务端下发的原始 TOML，供外部重载后重新叠加锁定时复用。 */
	private static final Map<FileKey, CommentedConfig> syncedConfigs = new HashMap<>();
	private static boolean receivedSync = false;
	/** 定位一条配置项的键：哪份文件的哪个路径。 */
	private record LockKey(FileKey file, String path) {}
	/** 更新服务端锁定的配置：解析 TOML → 用 ConfigSerializer 获取类型化值 → 应用锁定。 */
	public static void setLocks(String modId, String fileName, String tomlContent) {
		// 忽略玩家已断开连接后的延迟包
		if (ClientUtil.mc.getConnection() == null && !ClientUtil.mc.isSingleplayer()) return;
		var key = new FileKey(modId, fileName);
		if (!ConfigRegistry.isLockable(key)) return;
		receivedSync = true;
		// 解析 TOML
		CommentedConfig tomlConfig;
		try {
			tomlConfig = new TomlParser().parse(new StringReader(tomlContent));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return;
		}
		// 提取扁平路径→值映射（用于锁定追踪和 revert）
		var newLocks = new HashMap<String, String>();
		collectPaths(tomlConfig, "", newLocks);
		var handler = ConfigRegistry.getHandler(key).cast();
		var root = handler.getConfigTree();
		var config = handler.getConfig();
		// 恢复此文件上已被移除的锁定
		var oldLocks = lockedConfigs.getOrDefault(key, Map.of());
		for (var oldEntry : oldLocks.entrySet()) {
			if (newLocks.containsKey(oldEntry.getKey())) continue;
			var original = originalValues.remove(new LockKey(key, oldEntry.getKey()));
			if (original == null) continue;
			var node = root.getValueNode(oldEntry.getKey());
			if (node == null) continue;
			ConfigRegistry.applyLockedValue(key, node, original);
			node.resetToActive(config);
		}
		// 备份加锁前的原值（只记第一次，之后重复同步不改基线）
		for (var path : newLocks.keySet()) {
			var node = root.getValueNode(path);
			if (node == null) continue;
			originalValues.putIfAbsent(new LockKey(key, path), node.getActiveValue(config));
		}
		lockedConfigs.put(key, newLocks);
		syncedConfigs.put(key, tomlConfig);
		reapply(key);
	}
	/** 配置文件被外部重载后，重新叠加服务端锁定的值。 */
	public static void reapply(FileKey key) {
		var lockedPaths = lockedConfigs.get(key);
		var tomlConfig = syncedConfigs.get(key);
		if (lockedPaths == null || lockedPaths.isEmpty() || tomlConfig == null || !ConfigRegistry.isLockable(key)) return;
		var handler = ConfigRegistry.getHandler(key).cast();
		var root = handler.getConfigTree();
		Object instance;
		try {
			instance = handler.getSerializer().deserializeFrom(tomlConfig);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return;
		}
		var config = handler.getConfig();
		for (var path : lockedPaths.keySet()) {
			var node = root.getValueNode(path);
			if (node == null) continue;
			var typedValue = node.getActiveValue(instance);
			if (typedValue == null) continue;
			ConfigRegistry.applyLockedValue(key, node, typedValue);
			node.resetToActive(config);
		}
	}
	/** 递归遍历 CommentedConfig，收集扁平路径→字符串值映射。 */
	private static void collectPaths(CommentedConfig config, String prefix, Map<String, String> result) {
		config.entrySet().forEach(entry -> {
			var key = entry.getKey();
			var value = entry.getValue();
			var path = prefix.isEmpty() ? key : prefix + "." + key;
			if (value instanceof CommentedConfig sub) collectPaths(sub, path, result);
			else result.put(path, String.valueOf(value));
		});
	}
	// -- 待处理的锁定操作（延迟至保存时发送） --
	@Nullable
	public static Boolean getPendingLock(FileKey file, String configId) {
		return pendingLocks.get(new LockKey(file, configId));
	}
	public static void setPendingLock(FileKey file, String configId, boolean shouldLock) {
		var key = new LockKey(file, configId);
		if (!pendingLocks.containsKey(key)) pendingLocks.put(key, shouldLock);
		else pendingLocks.remove(key);
	}
	public static void clearPendingLock(FileKey file, String configId) {
		pendingLocks.remove(new LockKey(file, configId));
	}
	public static boolean hasPendingLocks() {
		return !pendingLocks.isEmpty();
	}
	public static void clearPendingLocks() {
		pendingLocks.clear();
	}
	/** 把所有待处理的锁定/解锁请求发送给服务端。 */
	public static void flushPendingLocks() {
		if (pendingLocks.isEmpty()) return;
		if (ClientUtil.mc.player == null) {
			pendingLocks.clear();
			return;
		}
		for (var entry : new ArrayList<>(pendingLocks.entrySet())) {
			var key = entry.getKey();
			if (!ConfigRegistry.isLockable(key.file())) continue;
			String value;
			if (entry.getValue()) value = getCurrentValueAsString(key.file(), key.path());
			else {
				value = "";
				updateOriginalValue(key.file(), key.path());
			}
			Platform.get().sendToServer(new ConfigLockPayload(key.file().modId(), key.file().fileName(), key.path(), value));
		}
		pendingLocks.clear();
	}
	private static @NotNull String getCurrentValueAsString(FileKey file, String configId) {
		var node = ConfigRegistry.getRootConfigNode(file).getValueNode(configId);
		var config = ConfigRegistry.getActiveConfig(file);
		if (node == null || config == null) return "";
		var value = node.getActiveValue(config);
		return switch (value) {
			case null -> "";
			case Integer integer when node.isColorValue() -> String.format(node.colorHasAlpha() ? "0x%08X" : "0x%06X", integer);
			case Enum<?> anEnum -> anEnum.name();
			case Point point -> point.x + ", " + point.y;
			default -> value.toString();
		};
	}
	private static void updateOriginalValue(FileKey file, String configId) {
		var node = ConfigRegistry.getRootConfigNode(file).getValueNode(configId);
		var config = ConfigRegistry.getActiveConfig(file);
		if (node == null || config == null) return;
		originalValues.put(new LockKey(file, configId), node.getActiveValue(config));
	}
	// -- 查询方法 --
	public static boolean hasReceivedSync() {
		return receivedSync;
	}
	public static void clear() {
		ConfigRegistry.getRegisteredFileKeys().forEach(ConfigRegistry::resetToSaved);
		originalValues.clear();
		lockedConfigs.clear();
		syncedConfigs.clear();
		pendingLocks.clear();
		receivedSync = false;
	}
	public static boolean isLocked(FileKey file, String configId) {
		var fileLocks = lockedConfigs.get(file);
		return fileLocks != null && fileLocks.containsKey(configId);
	}
}
