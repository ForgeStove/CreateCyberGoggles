package io.github.forgestove.create_cyber_goggles.config.server;
import io.github.forgestove.create_cyber_goggles.config.ConfigRegistry;
import io.github.forgestove.create_cyber_goggles.config.network.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.network.PacketDistributor;
/** 处理配置锁管理的服务器生命周期事件。 */
public final class ServerLifecycleHandler {
	/** 服务器启动时：从磁盘加载锁定存储。 */
	public static void onServerStarting(ServerStartingEvent event) {
		var lockStore = new ServerConfigLockStore(event.getServer(), ConfigRegistry.getModId());
		lockStore.load();
		ConfigNetwork.setLockStore(lockStore);
	}
	/** 服务器停止时：将锁定存储保存到磁盘。 */
	public static void onServerStopping(ServerStoppingEvent ignoredEvent) {
		var lockStore = ConfigNetwork.getLockStore();
		if (lockStore == null) return;
		lockStore.save();
		ConfigNetwork.clearLockStore();
	}
	/** 玩家登录时：向该玩家发送当前锁定的配置（如果他们安装了该模组）。 */
	public static void onPlayerLogin(PlayerLoggedInEvent event) {
		var lockStore = ConfigNetwork.getLockStore();
		if (lockStore == null) return;
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		// 仅向安装了此模组的客户端发送 — 否则NeoForge会抛出异常。
		if (!player.connection.hasChannel(ConfigSyncPayload.TYPE)) return;
		PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(lockStore.toTomlString()));
	}
}
