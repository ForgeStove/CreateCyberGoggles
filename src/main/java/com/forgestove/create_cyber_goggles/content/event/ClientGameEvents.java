package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
@Mod.EventBusSubscriber(modid = CreateCyberGoggles.ID, value = Dist.CLIENT, bus = Bus.FORGE)
public class ClientGameEvents {
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
}
