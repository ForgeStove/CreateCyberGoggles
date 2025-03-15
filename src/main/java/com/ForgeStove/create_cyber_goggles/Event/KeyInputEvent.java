package com.ForgeStove.create_cyber_goggles.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent.*;
import org.lwjgl.glfw.GLFW;
@EventBusSubscriber public class KeyInputEvent {
	public static double scrollDeltaY = 0;
	public static short scrollKeyboard = 0;
	@SubscribeEvent public static void onMouseScroll(MouseScrollingEvent event) {
		scrollDeltaY = event.getScrollDeltaY();
	}
	@SubscribeEvent public static void onKeyInput(Key event) {
		if (event.getAction() == GLFW.GLFW_PRESS) switch (event.getKey()) {
			case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_UP -> scrollKeyboard = -1;
			case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_RIGHT -> scrollKeyboard = 1;
		}
	}
}
