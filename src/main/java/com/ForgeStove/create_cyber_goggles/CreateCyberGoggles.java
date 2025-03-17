package com.ForgeStove.create_cyber_goggles;
import com.ForgeStove.create_cyber_goggles.config.Config;
import com.ForgeStove.create_cyber_goggles.event.KeyInputEvent;
import com.ForgeStove.create_cyber_goggles.render.OverlayRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
@Mod(CreateCyberGoggles.MOD_ID) public class CreateCyberGoggles {
	public static final String MOD_ID = "create_cyber_goggles";
	public CreateCyberGoggles(ModContainer modContainer) {
		registerConfigs(modContainer);
	}
	private static void registerConfigs(ModContainer modContainer) {
		modContainer.registerConfig(Type.CLIENT, Config.CONFIG_SPEC);
	}
	@EventBusSubscriber public static class KeyInputEvents {
		@SubscribeEvent public static void onKeyInput(MouseScrollingEvent event) {
			KeyInputEvent.onMouseScroll(event);
		}
	}
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD) public static class ClientBusEvents {
		@SubscribeEvent public static void configScreen(FMLLoadCompleteEvent event) {
			ModContainer modContainer = ModList.get().getModContainerById(MOD_ID).orElseThrow();
			Supplier<IConfigScreenFactory> configScreen = () -> ConfigurationScreen::new;
			modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
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
