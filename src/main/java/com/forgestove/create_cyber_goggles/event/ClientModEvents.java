package com.forgestove.create_cyber_goggles.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
@Mod.EventBusSubscriber(modid = CreateCyberGoggles.ID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientModEvents {
	@SubscribeEvent
	public static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
		ModKeyMapping.register(event);
	}
	@SubscribeEvent
	public static void registerGuiLayersEvent(RegisterGuiOverlaysEvent event) {
		OverlayRenderer.register(event);
	}
}
