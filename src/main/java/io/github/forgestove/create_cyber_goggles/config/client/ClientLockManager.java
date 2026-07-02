package io.github.forgestove.create_cyber_goggles.config.client;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.config.*;
import io.github.forgestove.create_cyber_goggles.config.network.*;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.awt.Point;
import java.io.StringReader;
import java.util.*;
/**
 * 追踪服务端锁定的配置项。
 * <p>
 * 在服务端发送 {@link ConfigSyncPayload} 时更新。
 * 锁定前备份原始值，断开连接或解锁时恢复。
 * 锁定值以 TOML 格式传输，由 {@link ConfigSerializer} 处理类型转换。
 * </p>
 */
public final class ClientLockManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<String, Boolean> pendingLocks = new HashMap<>();
	private static final Map<String, Object> originalValues = new HashMap<>();
	private static Map<String, String> lockedConfigs = Collections.emptyMap();
	private static boolean receivedSync = false;
	/** 更新服务端锁定的配置：解析 TOML → 用 ConfigSerializer 获取类型化值 → 应用锁定。 */
	@SuppressWarnings("unchecked")
	public static void setLocks(String modId, String tomlContent) {
		// 忽略玩家已断开连接后的延迟包
		if (Minecraft.getInstance().getConnection() == null && !Minecraft.getInstance().isSingleplayer()) return;
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
		// 使用 ConfigSerializer 获取类型化配置实例
		var handler = (ConfigHandler<Object>) Config.getHandler(modId);
		if (handler == null) return;
		var root = handler.getConfigTree();
		if (root == null) return;
		Object instance;
		try {
			instance = handler.getSerializer().deserializeFrom(tomlConfig);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return;
		}
		// 恢复已移除的锁定
		var oldLocks = lockedConfigs;
		for (var oldEntry : oldLocks.entrySet()) {
			if (newLocks.containsKey(oldEntry.getKey())) continue;
			var original = originalValues.remove(oldEntry.getKey());
			if (original == null) continue;
			var node = root.getValueNode(oldEntry.getKey());
			if (node != null) Config.applyLockedValue(modId, node, original);
			if (node == null) continue;
			node.resetToActive(handler.getConfig());
		}
		// 备份并应用锁定
		for (var entry : newLocks.entrySet()) {
			var node = root.getValueNode(entry.getKey());
			if (node == null) continue;
			if (!originalValues.containsKey(entry.getKey())) {
				var config = handler.getConfig();
				if (config == null) continue;
				originalValues.put(entry.getKey(), node.getActiveValue(config));
			}
			var typedValue = node.getActiveValue(instance);
			if (typedValue != null) {
				Config.applyLockedValue(modId, node, typedValue);
				node.resetToActive(handler.getConfig());
			}
		}
		lockedConfigs = newLocks;
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
	public static Boolean getPendingLock(String configId) {
		return pendingLocks.get(configId);
	}
	public static void setPendingLock(String configId, boolean shouldLock) {
		if (!pendingLocks.containsKey(configId)) pendingLocks.put(configId, shouldLock);
		else pendingLocks.remove(configId);
	}
	public static void clearPendingLock(String configId) {
		pendingLocks.remove(configId);
	}
	public static boolean hasPendingLocks() {
		return !pendingLocks.isEmpty();
	}
	public static void clearPendingLocks() {
		pendingLocks.clear();
	}
	public static void flushPendingLocks(String modId) {
		if (pendingLocks.isEmpty()) return;
		var mc = Minecraft.getInstance();
		if (mc.player == null) {
			pendingLocks.clear();
			return;
		}
		var root = Config.getRootConfigNode(modId);
		for (var entry : pendingLocks.entrySet()) {
			var configId = entry.getKey();
			var shouldLock = entry.getValue();
			String value;
			if (shouldLock) value = getCurrentValueAsString(modId, root, configId);
			else {
				value = "";
				updateOriginalValue(modId, root, configId);
			}
			PacketDistributor.sendToServer(new ConfigLockPayload(configId, value));
		}
		pendingLocks.clear();
	}
	@SuppressWarnings("unchecked")
	private static @NotNull String getCurrentValueAsString(String modId, @Nullable RootConfigNode<?> root, String configId) {
		if (root == null) return "";
		var node = root.getValueNode(configId);
		if (node == null) return "";
		var config = Config.getActiveConfig(modId);
		if (config == null) return "";
		var value = ((ValueConfigNode<Object, Object>) node).getActiveValue(config);
		return switch (value) {
			case null -> "";
			case Integer integer when node.isColorValue() -> String.format(node.colorHasAlpha() ? "0x%08X" : "0x%06X", integer);
			case Enum<?> anEnum -> anEnum.name();
			case Point point -> point.x + ", " + point.y;
			default -> value.toString();
		};
	}
	@SuppressWarnings("unchecked")
	private static void updateOriginalValue(String modId, @Nullable RootConfigNode<?> root, String configId) {
		if (root == null) return;
		var node = root.getValueNode(configId);
		if (node == null) return;
		var config = Config.getActiveConfig(modId);
		if (config == null) return;
		originalValues.put(configId, ((ValueConfigNode<Object, Object>) node).getActiveValue(config));
	}
	// -- 查询方法 --
	public static boolean hasReceivedSync() {
		return receivedSync;
	}
	public static void clear() {
		Config.resetToSaved(Config.getModId());
		originalValues.clear();
		lockedConfigs = Collections.emptyMap();
		pendingLocks.clear();
		receivedSync = false;
	}
	public static boolean isLocked(String configId) {
		return lockedConfigs.containsKey(configId);
	}
}
