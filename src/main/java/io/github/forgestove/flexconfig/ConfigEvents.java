package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.ConfigEditBox;
import io.github.forgestove.flexconfig.client.gui.ConfigScreen;
import io.github.forgestove.flexconfig.network.ConfigNetwork;
import io.github.forgestove.flexconfig.server.ServerLifecycleHandler;
import io.github.forgestove.flexconfig.server.ServerLockApplier;
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

import java.util.*;
@EventBusSubscriber
public final class ConfigEvents {
	/** 服务端侧的热重载轮询计数。 */
	private static int serverWatchTicks;
	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		ConfigNetwork.register(event);
	}
	/** 服务端 tick：SERVER 型配置的外部改动热重载。双端都要跑，单人存档也走这里。 */
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		if (++serverWatchTicks % ConfigWatchService.INTERVAL_TICKS != 0) return;
		for (var key : ConfigWatchService.poll(Set.of(), ConfigType.SERVER)) {
			ConfigRegistry.getHandler(key).reloadAndCopy();
			ServerLockApplier.reapply(key);   // 重载会抹掉锁值，需要重新叠加
		}
	}
	/** 世界即将开始：加载全部 SERVER 型配置（存档目录优先）。双端都要跑，单人存档也走这里。 */
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		ConfigRegistry.loadServerConfigs(event.getServer());
	}
	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		ConfigRegistry.applyWorldRestart();   // 退世界即「世界重启」，先让 WORLD 型改动生效
		ConfigRegistry.unloadServerConfigs();
	}
	@EventBusSubscriber(Dist.CLIENT)
	public static class Client {
		private static final Map<String, Boolean> PREVIOUS_STATES = new HashMap<>();
		/** 客户端侧的热重载轮询计数。 */
		private static int watchTicks;
		@SubscribeEvent
		public static void register(Pre event) {
			ConfigEditBox.onScreenMouseClicked(event);
		}
		@SubscribeEvent
		public static void onClientTick(Post event) {
			watchConfigFiles();
			if (ClientUtil.mc.screen != null) return;
			var window = ClientUtil.mc.getWindow().getWindow();
			// 快捷键可能挂在模组的任意一份配置上，按文件遍历
			ConfigRegistry.getRegisteredFileKeys()
				.forEach(key -> ConfigRegistry.getHandler(key).tickTriggerKeybinds(window, PREVIOUS_STATES));
		}
		/** 非 SERVER 型配置的外部改动热重载（SERVER 型归服务端 tick 处理）。 */
		private static void watchConfigFiles() {
			if (++watchTicks % ConfigWatchService.INTERVAL_TICKS != 0) return;
			// 界面正在编辑的那份跳过（免得冲掉未保存的编辑），其余文件照常热重载
			var editing = ClientUtil.mc.screen instanceof ConfigScreen<?, ?> screen ? screen.editedFiles() : Set.<FileKey>of();
			for (var key : ConfigWatchService.poll(editing, ConfigType.CLIENT, ConfigType.COMMON)) {
				ConfigRegistry.getHandler(key).reloadAndCopy();
				ClientLockManager.reapply(key);
			}
		}
		@SubscribeEvent
		public static void onLoggingOut(LoggingOut event) {
			ClientServerConfigCache.clear();
			ClientLockManager.clear();
			ConfigRegistry.applyWorldRestart();   // 退出世界即「世界重启」，让 WORLD 型改动生效
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
