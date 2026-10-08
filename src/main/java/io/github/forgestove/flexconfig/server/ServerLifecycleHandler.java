package io.github.forgestove.flexconfig.server;
import io.github.forgestove.flexconfig.ConfigRegistry;
import io.github.forgestove.flexconfig.network.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.server.*;
/** 处理配置锁管理的服务器生命周期事件。 */
public final class ServerLifecycleHandler {
	/** 服务器启动时：为每份「可锁定」的配置（非 SERVER 型）从磁盘加载锁定存储。 */
	public static void onServerStarting(ServerStartingEvent event) {
		ConfigRegistry.getRegisteredFileKeys().forEach(key -> {
			if (!ConfigRegistry.isLockable(key)) return;
			var lockStore = new ServerConfigLockStore(event.getServer(), key.modId(), key.fileName());
			lockStore.load();
			ConfigNetwork.setLockStore(key, lockStore);
			ServerLockApplier.applyAll(key, lockStore);
		});
	}
	/** 服务器停止时：将所有模组的锁定存储保存到磁盘。 */
	public static void onServerStopping(ServerStoppingEvent ignoredEvent) {
		ConfigNetwork.saveAll();
		ConfigNetwork.clearAll();
	}
	/** 玩家登录时：向该玩家发送所有模组当前锁定的配置（如果他们安装了该模组）。 */
	public static void onPlayerLogin(PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		// 仅向安装了此模组的客户端发送 — 否则NeoForge会抛出异常。
		if (!player.connection.hasChannel(ConfigSyncPayload.TYPE)) return;
		ConfigNetwork.sendLocksToPlayer(player);
		ConfigNetwork.sendServerConfigsToPlayer(player);
	}
}
