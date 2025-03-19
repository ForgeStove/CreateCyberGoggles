package com.ForgeStove.create_cyber_goggles;
import com.ForgeStove.create_cyber_goggles.event.*;
import com.ForgeStove.create_cyber_goggles.render.OverlayRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.*;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.event.InputEvent.*;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.*;
import org.jetbrains.annotations.NotNull;
@Mod(CreateCyberGoggles.ID) public class CreateCyberGoggles {
	public static final String ID = "create_cyber_goggles";
	public static final String NAME = "Create: Cyber Goggles";
	public CreateCyberGoggles(@NotNull ModContainer modContainer) {
		modContainer.registerConfig(Type.CLIENT, Config.CLIENT_SPEC);
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.GAME) static class ClientGameEvents {
		@SubscribeEvent static void onKeyInput(Key event) {
			KeyInputEvent.onKeyInput(event);
		}
		@SubscribeEvent static void onMouseScroll(MouseScrollingEvent event) {
			KeyInputEvent.onMouseScroll(event);
		}
	}
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD) static class ClientModEvents {
		@SubscribeEvent static void registerKeyMappings(RegisterKeyMappingsEvent event) {
			KeyBinds.register(event);
		}
		@SubscribeEvent static void registerGuiOverlays(@NotNull RegisterGuiLayersEvent event) {
			OverlayRenderer.register(event);
		}
	}
}
