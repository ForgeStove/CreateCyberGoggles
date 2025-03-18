package com.ForgeStove.create_cyber_goggles;
import com.ForgeStove.create_cyber_goggles.event.*;
import com.ForgeStove.create_cyber_goggles.render.OverlayRenderer;
import net.minecraft.resources.ResourceLocation;
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

import java.util.function.Supplier;
@Mod(CreateCyberGoggles.MOD_ID) public class CreateCyberGoggles {
	public static final String MOD_ID = "create_cyber_goggles";
	public static final String NAME = "Create: Cyber Goggles";
	public CreateCyberGoggles(ModContainer modContainer) {
		modContainer.registerConfig(Type.CLIENT, Config.CLIENT_SPEC);
		Supplier<IConfigScreenFactory> configScreen = () -> ConfigurationScreen::new;
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
	}
	@EventBusSubscriber public static class CommonBusEvents {
		@SubscribeEvent public static void onMouseScroll(MouseScrollingEvent event) {
			KeyInputEvent.onMouseScroll(event);
		}
		@SubscribeEvent public static void onKeyInput(Key event) {
			KeyInputEvent.onKeyInput(event);
		}
	}
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD) public static class ClientBusEvents {
		@SubscribeEvent public static void register(RegisterKeyMappingsEvent event) {
			KeyBinds.register(event);
		}
		@SubscribeEvent public static void registerGuiOverlays(@NotNull RegisterGuiLayersEvent event) {
			event.registerAbove(
					VanillaGuiLayers.HOTBAR,
					ResourceLocation.fromNamespaceAndPath(MOD_ID, "goggle"),
					OverlayRenderer.OVERLAY
			);
		}
	}
}
