package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.event.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.*;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.InputEvent.*;
import net.neoforged.neoforge.client.event.*;
@Mod(value = CreateCyberGoggles.ID, dist = Dist.CLIENT)
public class CreateCyberGoggles {
	public static final String ID = "create_cyber_goggles";
	public CreateCyberGoggles(ModContainer container) {
		CCGConfig.register(container);
	}
	@EventBusSubscriber(modid = ID, value = Dist.CLIENT, bus = Bus.GAME)
	public static class ClientGameEvents {
		@SubscribeEvent
		public static void key(Key event) {
			KeyInput.toggleDiving();
			KeyInput.openConfigScreen();
			KeyInput.openStockScreen();
			KeyInput.previewFilterScreen();
		}
		@SubscribeEvent
		public static void mouseScrollingEvent(MouseScrollingEvent event) {
			MouseScroll.onMouseScroll(event);
		}
		@SubscribeEvent
		public static void tick(Post event) {
			KineticEffector.tick();
			KineticDebugger.tick();
		}
	}
	@EventBusSubscriber(modid = ID, value = Dist.CLIENT, bus = Bus.MOD)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
			CCGKeyMapping.register(event);
		}
		@SubscribeEvent
		public static void registerGuiLayersEvent(RegisterGuiLayersEvent event) {
			OverlayRenderer.register(event);
		}
	}
}
