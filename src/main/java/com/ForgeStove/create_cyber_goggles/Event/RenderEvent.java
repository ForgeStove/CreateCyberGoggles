package com.ForgeStove.create_cyber_goggles.Event;
import com.ForgeStove.create_cyber_goggles.*;
import com.ForgeStove.create_cyber_goggles.Render.OverlayRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD) public class RenderEvent {
	@SubscribeEvent public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
		event.registerAbove(
				VanillaGuiLayers.HOTBAR,
				ResourceLocation.fromNamespaceAndPath(CreateCyberGoggles.MOD_ID, "goggle"),
				OverlayRenderer.OVERLAY
		);
	}
}
