package io.github.forgestove.config.server;
import io.github.forgestove.config.ConfigRegistry;
import io.github.forgestove.config.network.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.server.*;
/** 处理配置锁管理的服务器生命周期事件。 */
public final class ServerLifecycleHandler {
	/** 服务器启动时：为每个已注册模组从磁盘加载锁定存储。 */
	public static void onServerStarting(ServerStartingEvent event) {
		ConfigRegistry.getRegisteredModIds().forEach(modId -> {
			var lockStore = new ServerConfigLockStore(event.getServer(), modId);
			lockStore.load();
			ConfigNetwork.setLockStore(modId, lockStore);
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
	}
}
