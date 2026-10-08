package io.github.forgestove.flexconfig.server;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.ConfigRegistry;
import io.github.forgestove.flexconfig.FileKey;
import io.github.forgestove.flexconfig.network.ConfigNetwork;
import org.slf4j.Logger;
/**
 * 把服务端锁定的配置项应用到服务端自身的活动配置。
 * <p>
 * 与客户端的 {@code ClientLockManager} 对等：锁不只是客户端补丁，服务端进程读到的也是锁定值。
 * 每次都先从 TOML 重载以撤销旧锁值，再逐条应用当前锁，因此解锁后能恢复原值。
 */
public final class ServerLockApplier {
	private static final Logger LOGGER = LogUtils.getLogger();
	/** 从 TOML 重载该配置后应用全部锁定；解锁能因此恢复原值。 */
	public static void applyAll(FileKey key, ServerConfigLockStore store) {
		if (!ConfigRegistry.getRegisteredFileKeys().contains(key)) return;
		ConfigRegistry.resetToSaved(key);
		apply(key, store);
	}
	/** 配置文件被外部重载后，重新叠加锁定值（调用方已重读文件，这里不再读盘）。 */
	public static void reapply(FileKey key) {
		var store = ConfigNetwork.getLockStore(key);
		if (store == null) return;
		apply(key, store);
	}
	private static void apply(FileKey key, ServerConfigLockStore store) {
		if (!ConfigRegistry.isLockable(key)) return;
		var paths = store.lockedPaths();
		if (paths.isEmpty()) return;
		var handler = ConfigRegistry.getHandler(key).cast();
		Object locked;
		try {
			locked = handler.getSerializer().deserializeFrom(store.toConfig());
		} catch (Exception e) {
			LOGGER.error("Failed to apply config locks for '{}'", key, e);
			return;
		}
		var root = handler.getConfigTree();
		for (var path : paths) {
			var node = root.getValueNode(path);
			if (node == null) continue;
			var value = node.getActiveValue(locked);
			if (value == null) continue;
			ConfigRegistry.applyLockedValue(key, node, value);
		}
	}
}
