package io.github.forgestove.flexconfig.network;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.ConfigScreen;
import io.github.forgestove.flexconfig.server.ServerConfigLockStore;
import io.github.forgestove.flexconfig.server.ServerLockApplier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
public final class ConfigNetwork {
	private static final Logger LOGGER = LogUtils.getLogger();
	/** 每个 (模组, 配置文件) 一份锁定存储。 */
	private static final Map<FileKey, ServerConfigLockStore> lockStores = new HashMap<>();
	public static void register(RegisterPayloadHandlersEvent event) {
		var registrar = event.registrar(FlexConfig.ID).optional();
		registrar.playToServer(ConfigLockPayload.TYPE, ConfigLockPayload.STREAM_CODEC, ConfigNetwork::handleConfigLockServer)
			.playToClient(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC, ConfigNetwork::handleConfigSyncClient)
			.playToClient(ServerConfigPayload.TYPE, ServerConfigPayload.STREAM_CODEC, ConfigNetwork::handleServerConfigClient);
	}
	// -- 数据包处理器 --
	private static void handleConfigLockServer(ConfigLockPayload payload, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer player)) return;
		if (!player.hasPermissions(2)) return;
		var key = new FileKey(payload.modId(), payload.fileName());
		var lockStore = lockStores.get(key);
		if (lockStore == null) return;
		if (payload.value().isEmpty()) lockStore.unlockEntry(payload.configId());
		else lockStore.lockEntry(payload.configId(), payload.value());
		ServerLockApplier.applyAll(key, lockStore);
		broadcastLocks();
	}
	private static void handleConfigSyncClient(ConfigSyncPayload payload, IPayloadContext context) {
		ClientLockManager.setLocks(payload.modId(), payload.fileName(), payload.tomlContent());
		context.enqueueWork(() -> {if (ClientUtil.mc.screen instanceof ConfigScreen<?, ?> configScreen) configScreen.refresh();});
	}
	private static void handleServerConfigClient(ServerConfigPayload payload, IPayloadContext context) {
		ClientServerConfigCache.accept(payload.modId(), payload.fileName(), payload.tomlContent());
	}
	private static void broadcastLocks() {
		if (lockStores.isEmpty()) return;
		lockStores.forEach((key, value) -> PacketDistributor.sendToAllPlayers(new ConfigSyncPayload(
			key.modId(),
			key.fileName(),
			value.toTomlString()
		)));
	}
	public static void setLockStore(FileKey key, ServerConfigLockStore store) {
		lockStores.put(key, store);
	}
	/** 获取某份配置的锁定存储；未注册或非服务端时为 {@code null}。 */
	public static @Nullable ServerConfigLockStore getLockStore(FileKey key) {
		return lockStores.get(key);
	}
	public static void saveAll() {
		lockStores.values().forEach(ServerConfigLockStore::save);
	}
	public static void clearAll() {
		lockStores.clear();
	}
	public static void sendLocksToPlayer(ServerPlayer player) {
		lockStores.forEach((key, value) -> PacketDistributor.sendToPlayer(
			player,
			new ConfigSyncPayload(key.modId(), key.fileName(), value.toTomlString())
		));
	}
	/** 把全部 SERVER 型配置的当前内容（文件值，不含锁）下发给该玩家。 */
	public static void sendServerConfigsToPlayer(ServerPlayer player) {
		ConfigRegistry.getRegisteredFileKeys().forEach(key -> {
			var handler = ConfigRegistry.getHandler(key);
			if (handler.getType() != ConfigType.SERVER) return;
			String tomlContent;
			try {
				var typed = handler.cast();
				tomlContent = typed.getSerializer().writeToString(typed.getSavedConfig());
			} catch (SerializationException e) {
				LOGGER.error("Failed to serialize server config '{}'", key, e);
				return;
			}
			PacketDistributor.sendToPlayer(player, new ServerConfigPayload(key.modId(), key.fileName(), tomlContent));
		});
	}
}
