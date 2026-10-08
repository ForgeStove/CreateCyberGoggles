package io.github.forgestove.flexconfig.server;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.ConfigSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.nio.file.*;
import java.util.*;
/**
 * 按世界存档存储锁定的配置条目。
 * <p>
 * 持久化为存档 {@code serverconfig/flexconfig/<配置文件名>_locks.toml}，结构与配置文件一致（嵌套子表），
 * 用与配置相同的 {@link TomlWriter} 原子写出。
 * 生命周期由 {@link ServerLifecycleHandler} 管理。
 */
public final class ServerConfigLockStore {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Path lockFilePath;
	/** 旧命名（只按 modId 而不分文件、也不在 flexconfig 子目录）的锁文件，仅在迁移时读取。 */
	private final Path legacyLockFilePath;
	/** 锁定项，结构与配置文件一致；键由客户端传来的路径按点号分层。 */
	private CommentedConfig locks = ConfigSerializer.inMemory();
	public ServerConfigLockStore(MinecraftServer server, String modId, String fileName) {
		lockFilePath = server.getWorldPath(new LevelResource("serverconfig/flexconfig/" + lockBaseName(fileName) + "_locks.toml"));
		legacyLockFilePath = server.getWorldPath(new LevelResource("serverconfig/" + modId + "_locks.toml"));
	}
	/** 锁文件名去掉 .toml 后缀，例如 {@code create_cyber_goggles-client.toml} → {@code create_cyber_goggles-client}。 */
	private static String lockBaseName(String fileName) {
		return fileName.endsWith(".toml") ? fileName.substring(0, fileName.length() - ".toml".length()) : fileName;
	}
	/** 从磁盘加载锁定。在服务器启动时调用。 */
	public void load() {
		var path = lockFilePath;
		if (!Files.exists(path) && Files.exists(legacyLockFilePath)) {
			LOGGER.warn("Adopting legacy lock file {} for {}", legacyLockFilePath, lockFilePath.getFileName());
			path = legacyLockFilePath;
		}
		if (!Files.exists(path)) {
			locks = ConfigSerializer.inMemory();
			return;
		}
		try {
			var parsed = ConfigSerializer.read(path);
			locks = parsed == null ? ConfigSerializer.inMemory() : parsed;
			LOGGER.info("Loaded {} config lock(s)", lockedPaths().size());
		} catch (Exception e) {
			LOGGER.error("Failed to load config locks from {}", path, e);
			locks = ConfigSerializer.inMemory();
		}
	}
	/** 将配置条目锁定为特定的值字符串。 */
	public void lockEntry(String configId, String value) {
		locks.set(splitPath(configId), toTypedValue(value));
		save();
	}
	/** 解锁配置条目。将其从锁定存储中移除。 */
	public void unlockEntry(String configId) {
		locks.remove(splitPath(configId));
		save();
	}
	/** 将锁定保存到磁盘。在服务器停止和每次更改后调用。 */
	public void save() {
		try {
			Files.createDirectories(lockFilePath.getParent());
			new TomlWriter().write(locks, lockFilePath, WritingMode.REPLACE_ATOMIC);
		} catch (Exception e) {
			LOGGER.error("Failed to save config locks to {}", lockFilePath, e);
		}
	}
	/** 当前锁定的配置项路径（点分形式）的只读视图。 */
	public Set<String> lockedPaths() {
		var result = new LinkedHashSet<String>();
		collectPaths(locks, "", result);
		return Collections.unmodifiableSet(result);
	}
	/** 锁定项的结构化表示，供服务端直接应用（无需再走文本往返）。 */
	public CommentedConfig toConfig() {
		return locks;
	}
	/** 锁定项的 TOML 文本，供下发给客户端。 */
	public String toTomlString() {
		return new TomlWriter().writeToString(locks);
	}
	private static void collectPaths(UnmodifiableConfig config, String prefix, Set<String> out) {
		for (var entry : config.entrySet()) {
			var path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
			var value = entry.getValue();
			if (value instanceof UnmodifiableConfig sub) collectPaths(sub, path, out);
			else out.add(path);
		}
	}
	private static List<String> splitPath(String configId) {
		return List.of(configId.split("\\."));
	}
	/** 把客户端传来的字面量还原成 TOML 原生类型；不认识的一律当字符串。 */
	private static Object toTypedValue(String raw) {
		if (raw.equals("true")) return Boolean.TRUE;
		if (raw.equals("false")) return Boolean.FALSE;
		try {
			return Integer.parseInt(raw);
		} catch (NumberFormatException ignored) {}
		try {
			return Integer.decode(raw);   // 支持 0xFF0000 等十六进制颜色
		} catch (NumberFormatException ignored) {}
		try {
			return Long.parseLong(raw);
		} catch (NumberFormatException ignored) {}
		try {
			return Long.decode(raw);
		} catch (NumberFormatException ignored) {}
		try {
			return Double.parseDouble(raw);
		} catch (NumberFormatException ignored) {}
		return raw;
	}
}
