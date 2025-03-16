package com.ForgeStove.create_cyber_goggles;
import com.ForgeStove.create_cyber_goggles.config.Configs;
import com.ForgeStove.create_cyber_goggles.event.KeyInputEvent;
import com.ForgeStove.create_cyber_goggles.render.OverlayRenderer;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent.*;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
@Mod(CreateCyberGoggles.MOD_ID) public class CreateCyberGoggles {
	public static final String MOD_ID = "create_cyber_goggles";
	public CreateCyberGoggles(ModContainer modContainer) {
		registerConfigs(modContainer);
	}
	private static void registerConfigs(ModContainer modContainer) {
		Set<Map.Entry<Type, ConfigBase>> entries = Configs.registerConfigs();
		for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries)
			modContainer.registerConfig(entry.getKey(), entry.getValue().specification);
	}
	@EventBusSubscriber(bus = Bus.MOD) public static class ModBusEvents {
		@SubscribeEvent public static void onLoad(@NotNull Loading event) {
			Configs.onLoad(event.getConfig());
		}
		@SubscribeEvent public static void onReload(@NotNull Reloading event) {
			Configs.onReload(event.getConfig());
		}
	}
	@EventBusSubscriber public static class KeyInputEvents {
		@SubscribeEvent public static void onKeyInput(MouseScrollingEvent event) {
			KeyInputEvent.onMouseScroll(event);
		}
	}
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD) public static class ClientBusEvents {
		@SubscribeEvent public static void configScreen(FMLLoadCompleteEvent event) {
			ModContainer modContainer = ModList.get().getModContainerById(MOD_ID).orElseThrow();
			Supplier<IConfigScreenFactory> configScreen;
			configScreen = () -> (container, screen) -> new BaseConfigScreen(screen, MOD_ID);
			modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
			BaseConfigScreen.setDefaultActionFor(
					MOD_ID,
					base -> base.withButtonLabels("Client Settings", null, null)
							.withSpecs(Configs.client().specification, null, null)
			);
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
