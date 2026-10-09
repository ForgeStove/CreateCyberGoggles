package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;
/** {@link Platform} 的 NeoForge 实现。 */
public final class NeoForgePlatform implements Platform {
	@Override
	public Path configDir() {
		return FMLPaths.CONFIGDIR.get();
	}
	@Override
	public boolean isModLoaded(String modId) {
		return LoadingModList.get().getModFileById(modId) != null;
	}
	@Override
	public boolean isClient() {
		return FMLEnvironment.dist.isClient();
	}
	@Override
	public @Nullable String modDisplayName(String modId) {
		return ModList.get().getModContainerById(modId).map(container -> container.getModInfo().getDisplayName()).orElse(null);
	}
	@Override
	public void registerConfigScreen(String modId, Supplier<Screen> screenFactory) {
		ModList.get().getModContainerById(modId).ifPresent(container -> {
			Supplier<IConfigScreenFactory> extension = () -> (modContainer, screen) -> screenFactory.get();
			container.registerExtensionPoint(IConfigScreenFactory.class, extension);
		});
	}
	@Override
	public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}
	@Override
	public void sendToAllPlayers(CustomPacketPayload payload) {
		PacketDistributor.sendToAllPlayers(payload);
	}
	@Override
	public void sendToServer(CustomPacketPayload payload) {
		PacketDistributor.sendToServer(payload);
	}
	/** 由 {@link RegisterPayloadHandlersEvent} 的订阅者调用，把加载器的注册器交给核心。 */
	public static void registerPayloads(RegisterPayloadHandlersEvent event, Consumer<Payloads> consumer) {
		consumer.accept(new NeoForgePayloads(event.registrar(FlexConfig.ID).optional()));
	}
	private record NeoForgePayloads(PayloadRegistrar registrar) implements Payloads {
		@Override
		public <P extends CustomPacketPayload> void clientToServer(
			Type<P> type,
			StreamCodec<FriendlyByteBuf, P> codec,
			PayloadHandler<P> handler
		) {
			registrar.playToServer(type, codec, (payload, context) -> handler.handle(payload, new NeoForgeContext(context)));
		}
		@Override
		public <P extends CustomPacketPayload> void serverToClient(
			Type<P> type,
			StreamCodec<FriendlyByteBuf, P> codec,
			PayloadHandler<P> handler
		) {
			registrar.playToClient(type, codec, (payload, context) -> handler.handle(payload, new NeoForgeContext(context)));
		}
	}
	private record NeoForgeContext(IPayloadContext context) implements PayloadContext {
		@Override
		public @Nullable ServerPlayer player() {
			return context.player() instanceof ServerPlayer player ? player : null;
		}
		@Override
		public void enqueue(Runnable task) {
			context.enqueueWork(task);
		}
	}
}
