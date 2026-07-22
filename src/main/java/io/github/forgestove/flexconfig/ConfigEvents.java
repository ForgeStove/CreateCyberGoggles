package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.ConfigEditBox;
import io.github.forgestove.flexconfig.network.ConfigNetwork;
import io.github.forgestove.flexconfig.server.ServerLifecycleHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseButtonPressed.Pre;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.*;
@EventBusSubscriber
public class ConfigEvents {
	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		ConfigNetwork.register(event);
	}
	@EventBusSubscriber(Dist.CLIENT)
	public static class Client {
		private static final Map<String, Boolean> PREVIOUS_STATES = new HashMap<>();
		@SubscribeEvent
		public static void register(Pre event) {
			ConfigEditBox.onScreenMouseClicked(event);
		}
		@SubscribeEvent
		public static void onClientTick(Post event) {
			if (ClientUtil.mc.screen != null) return;
			var window = ClientUtil.mc.getWindow().getWindow();
			ConfigRegistry.getRegisteredModIds()
				.forEach(modId -> ConfigRegistry.getHandler(modId).tickTriggerKeybinds(window, PREVIOUS_STATES));
		}
		@SubscribeEvent
		public static void onLoggingOut(LoggingOut event) {
			ClientLockManager.clear();
		}
	}
	@EventBusSubscriber(Dist.DEDICATED_SERVER)
	public static class Server {
		@SubscribeEvent
		public static void onServerStarting(ServerStartingEvent event) {
			ServerLifecycleHandler.onServerStarting(event);
		}
		@SubscribeEvent
		public static void onServerStopping(ServerStoppingEvent event) {
			ServerLifecycleHandler.onServerStopping(event);
		}
		@SubscribeEvent
		public static void onPlayerLogin(PlayerLoggedInEvent event) {
			ServerLifecycleHandler.onPlayerLogin(event);
		}
	}
}
