package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.client.ClientLifecycleHandler;
import io.github.forgestove.flexconfig.network.ConfigNetwork;
import io.github.forgestove.flexconfig.platform.NeoForgePlatform;
import io.github.forgestove.flexconfig.server.ServerLifecycleHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseButtonPressed.Pre;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
/**
 * NeoForge 的事件订阅适配层：本文件只做「解包事件数据 → 转交核心」，不含任何业务逻辑。
 * <p>
 * 移植到别的加载器时，只需照着这里重写一遍订阅；对应的处理都在
 * {@link ClientLifecycleHandler} 与 {@link ServerLifecycleHandler} 里。
 */
@EventBusSubscriber
public final class ConfigEvents {
	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		NeoForgePlatform.registerPayloads(event, ConfigNetwork::register);
	}
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		ServerLifecycleHandler.onServerAboutToStart(event.getServer());
	}
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		ServerLifecycleHandler.onServerTick();
	}
	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		ServerLifecycleHandler.onServerStopped();
	}
	@EventBusSubscriber(Dist.CLIENT)
	public static class Client {
		@SubscribeEvent
		public static void register(Pre event) {
			ClientLifecycleHandler.onScreenMousePressed(event.getMouseX(), event.getMouseY());
		}
		@SubscribeEvent
		public static void onClientTick(Post event) {
			ClientLifecycleHandler.tick();
		}
		@SubscribeEvent
		public static void onLoggingOut(LoggingOut event) {
			ClientLifecycleHandler.onLoggingOut();
		}
	}
	@EventBusSubscriber(Dist.DEDICATED_SERVER)
	public static class Server {
		@SubscribeEvent
		public static void onServerStarting(ServerStartingEvent event) {
			ServerLifecycleHandler.onServerStarting(event.getServer());
		}
		@SubscribeEvent
		public static void onServerStopping(ServerStoppingEvent event) {
			ServerLifecycleHandler.onServerStopping();
		}
		@SubscribeEvent
		public static void onPlayerLogin(PlayerLoggedInEvent event) {
			if (event.getEntity() instanceof ServerPlayer player) ServerLifecycleHandler.onPlayerLogin(player);
		}
	}
}
