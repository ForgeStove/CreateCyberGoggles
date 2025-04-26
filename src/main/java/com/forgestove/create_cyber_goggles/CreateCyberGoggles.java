package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.config.Config;
import com.forgestove.create_cyber_goggles.content.event.*;
import com.forgestove.create_cyber_goggles.content.render.OverlayRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
@Mod(CreateCyberGoggles.ID)
public class CreateCyberGoggles {
	public static final String ID = "create_cyber_goggles";
	public static final String NAME = "Create: Cyber Goggles";
	public CreateCyberGoggles() {
		Config.register();
	}
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.FORGE)
	public static class ClientGameEvents {
		@SubscribeEvent
		public static void key(Key event) {KeyInput.onKeyInput(event);}
		@SubscribeEvent
		public static void mouseScrolling(MouseScrollingEvent event) {MouseScroll.onMouseScroll(event);}
	}
	@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
			KeyBind.register(event);
		}
		@SubscribeEvent
		public static void registerGuiLayersEvent(RegisterGuiOverlaysEvent event) {
			OverlayRenderer.register(event);
		}
	}
}
