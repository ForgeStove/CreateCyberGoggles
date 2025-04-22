package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.content.event.*;
import com.forgestove.create_cyber_goggles.content.render.OverlayRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.*;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.event.InputEvent.*;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.ScreenEvent.Closing;
import net.neoforged.neoforge.client.gui.*;
import org.jetbrains.annotations.NotNull;
@Mod(value = CreateCyberGoggles.ID, dist = Dist.CLIENT)
public class CreateCyberGoggles {
	public static final String ID = "create_cyber_goggles";
	public static final String NAME = "Create: Cyber Goggles";
	public CreateCyberGoggles(@NotNull ModContainer modContainer) {
		modContainer.registerConfig(Type.CLIENT, Config.CLIENT_SPEC);
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.GAME)
	public static class ClientGameEvents {
		@SubscribeEvent
		public static void key(Key event) {
			KeyInput.openFilterScreen(event);
			KeyInput.openConfigScreen(event);
			KeyInput.openStockScreen(event);
		}
		@SubscribeEvent
		public static void mouseScrollingEvent(MouseScrollingEvent event) {
			MouseScroll.onMouseScroll(event);
		}
		@SubscribeEvent
		public static void closing(Closing event) {CloseScreen.onCloseScreen(event);}
	}
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
			KeyBind.register(event);
		}
		@SubscribeEvent
		public static void registerGuiLayersEvent(RegisterGuiLayersEvent event) {
			OverlayRenderer.register(event);
		}
	}
}
