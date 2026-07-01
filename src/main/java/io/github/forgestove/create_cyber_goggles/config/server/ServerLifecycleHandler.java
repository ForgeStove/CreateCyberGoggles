package io.github.forgestove.create_cyber_goggles.config.server;
import io.github.forgestove.create_cyber_goggles.config.Config;
import io.github.forgestove.create_cyber_goggles.config.network.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.network.PacketDistributor;
/** Handles server lifecycle events for config lock management. */
public final class ServerLifecycleHandler {
	/** On server start: load the lock store from disk. */
	public static void onServerStarting(ServerStartingEvent event) {
		var lockStore = new ServerConfigLockStore(event.getServer(), Config.getModId());
		lockStore.load();
		ConfigNetwork.setLockStore(lockStore);
	}
	/** On server stop: save the lock store to disk. */
	public static void onServerStopping(ServerStoppingEvent ignoredEvent) {
		var lockStore = ConfigNetwork.getLockStore();
		if (lockStore == null) return;
		lockStore.save();
		ConfigNetwork.clearLockStore();
	}
	/** On player login: send the current locked configs to that player. */
	public static void onPlayerLogin(PlayerLoggedInEvent event) {
		var lockStore = ConfigNetwork.getLockStore();
		if (lockStore == null) return;
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(lockStore.toTomlString()));
	}
}
