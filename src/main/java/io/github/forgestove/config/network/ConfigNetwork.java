package io.github.forgestove.config.network;
import io.github.forgestove.config.*;
import io.github.forgestove.config.client.ClientLockManager;
import io.github.forgestove.config.client.gui.ConfigScreen;
import io.github.forgestove.config.server.ServerConfigLockStore;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;
public final class ConfigNetwork {
	private static final Map<String, ServerConfigLockStore> lockStores = new HashMap<>();
	public static void register(RegisterPayloadHandlersEvent event) {
		var registrar = event.registrar(ConfigRegistry.getFirstModId()).optional();
		registrar.playToServer(ConfigLockPayload.TYPE, ConfigLockPayload.STREAM_CODEC, ConfigNetwork::handleConfigLockServer);
		registrar.playToClient(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC, ConfigNetwork::handleConfigSyncClient);
	}
	// -- 数据包处理器 --
	private static void handleConfigLockServer(ConfigLockPayload payload, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player)) return;
		if (!player.hasPermissions(2)) return;
		var lockStore = lockStores.get(payload.modId());
		if (lockStore == null) return;
		if (payload.value().isEmpty()) lockStore.unlockEntry(payload.configId());
		else lockStore.lockEntry(payload.configId(), payload.value());
		broadcastLocks();
	}
	private static void handleConfigSyncClient(ConfigSyncPayload payload, IPayloadContext context) {
		ClientLockManager.setLocks(payload.modId(), payload.tomlContent());
		context.enqueueWork(() -> {if (Minecraft.getInstance().screen instanceof ConfigScreen<?, ?> configScreen) configScreen.refresh();});
	}
	private static void broadcastLocks() {
		if (lockStores.isEmpty()) return;
		lockStores.forEach((key, value) -> PacketDistributor.sendToAllPlayers(new ConfigSyncPayload(key, value.toTomlString())));
	}
	public static void setLockStore(String modId, ServerConfigLockStore store) {
		lockStores.put(modId, store);
	}
	public static void saveAll() {
		lockStores.values().forEach(ServerConfigLockStore::save);
	}
	public static void clearAll() {
		lockStores.clear();
	}
	public static void sendLocksToPlayer(ServerPlayer player) {
		lockStores.forEach((key, value) -> PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(key, value.toTomlString())));
	}
}
