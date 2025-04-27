package com.forgestove.create_cyber_goggles.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
@EventBusSubscriber(modid = CreateCyberGoggles.ID, value = Dist.CLIENT, bus = Bus.GAME)
public class ClientGameEvents {
	@SubscribeEvent
	public static void key() {
		KeyInput.toggleDiving();
		KeyInput.openConfigScreen();
		KeyInput.openStockScreen();
		KeyInput.openFilterScreen();
	}
	@SubscribeEvent
	public static void mouseScrollingEvent(MouseScrollingEvent event) {
		MouseScroll.onMouseScroll(event);
	}
}
