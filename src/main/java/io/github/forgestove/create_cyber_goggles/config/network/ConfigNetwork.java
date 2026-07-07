package io.github.forgestove.create_cyber_goggles.config.network;
import io.github.forgestove.create_cyber_goggles.config.ConfigRegistry;
import io.github.forgestove.create_cyber_goggles.config.client.ClientLockManager;
import io.github.forgestove.create_cyber_goggles.config.client.gui.ConfigScreen;
import io.github.forgestove.create_cyber_goggles.config.server.ServerConfigLockStore;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
public final class ConfigNetwork {
	@Nullable private static ServerConfigLockStore lockStore;
	public static void register(RegisterPayloadHandlersEvent event) {
		var registrar = event.registrar(ConfigNetwork.class.getPackageName()).optional();
		registrar.playToServer(ConfigLockPayload.TYPE, ConfigLockPayload.STREAM_CODEC, ConfigNetwork::handleConfigLockServer);
		registrar.playToClient(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC, ConfigNetwork::handleConfigSyncClient);
	}
	// -- 数据包处理器 --
	private static void handleConfigLockServer(ConfigLockPayload payload, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player)) return;
		if (!player.hasPermissions(2)) return;
		if (lockStore == null) return;
		if (payload.value().isEmpty()) lockStore.unlockEntry(payload.configId());
		else lockStore.lockEntry(payload.configId(), payload.value());
		broadcastLocks();
	}
	private static void handleConfigSyncClient(ConfigSyncPayload payload, IPayloadContext context) {
		ClientLockManager.setLocks(ConfigRegistry.getModId(), payload.tomlContent());
		context.enqueueWork(() -> {if (Minecraft.getInstance().screen instanceof ConfigScreen<?, ?> configScreen) configScreen.refresh();});
	}
	private static void broadcastLocks() {
		if (lockStore == null) return;
		PacketDistributor.sendToAllPlayers(new ConfigSyncPayload(lockStore.toTomlString()));
	}
	// -- 锁定存储管理（由ServerLifecycleHandler使用） --
	@Nullable
	public static ServerConfigLockStore getLockStore() {
		return lockStore;
	}
	public static void setLockStore(@Nullable ServerConfigLockStore store) {
		lockStore = store;
	}
	public static void clearLockStore() {
		lockStore = null;
	}
}
