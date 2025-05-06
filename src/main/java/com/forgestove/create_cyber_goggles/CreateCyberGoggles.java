package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.content.config.CyberConfig;
import com.forgestove.create_cyber_goggles.content.event.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.client.event.*;
import org.jetbrains.annotations.NotNull;
@Mod(value = CreateCyberGoggles.ID, dist = Dist.CLIENT)
public class CreateCyberGoggles {
	public static final String ID = "create_cyber_goggles";
	public CreateCyberGoggles(@NotNull ModContainer container) {
		CyberConfig.register(container);
	}
	@EventBusSubscriber(modid = ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
	public static class ClientGameEvents {
		@SubscribeEvent
		public static void key(InputEvent.Key event) {
			KeyInput.toggleDiving();
			KeyInput.openConfigScreen();
			KeyInput.openStockScreen();
			KeyInput.previewFilterScreen();
		}
		@SubscribeEvent
		public static void mouseScrollingEvent(InputEvent.MouseScrollingEvent event) {
			MouseScroll.onMouseScroll(event);
		}
	}
	@EventBusSubscriber(modid = ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
			ModKeyMapping.register(event);
		}
		@SubscribeEvent
		public static void registerGuiLayersEvent(RegisterGuiLayersEvent event) {
			OverlayRenderer.register(event);
		}
	}
}
