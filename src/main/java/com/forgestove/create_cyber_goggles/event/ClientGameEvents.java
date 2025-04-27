package com.forgestove.create_cyber_goggles.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
@Mod.EventBusSubscriber(modid = CreateCyberGoggles.ID, value = Dist.CLIENT, bus = Bus.FORGE)
public class ClientGameEvents {
	@SubscribeEvent
	public static void toggleDiving(Key event) {
		KeyInput.toggleDiving(event);
	}
	@SubscribeEvent
	public static void openConfigScreen(Key event) {
		KeyInput.openConfigScreen(event);
	}
	@SubscribeEvent
	public static void openStockScreen(Key event) {
		KeyInput.openStockScreen(event);
	}
	@SubscribeEvent
	public static void openFilterScreen(Key event) {
		KeyInput.openFilterScreen(event);
	}
	@SubscribeEvent
	public static void mouseScrollingEvent(MouseScrollingEvent event) {
		MouseScroll.onMouseScroll(event);
	}
}
