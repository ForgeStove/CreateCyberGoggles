package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.event.*;
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
	public CreateCyberGoggles() {
		CCGConfig.register();
	}
	@EventBusSubscriber(modid = ID, value = Dist.CLIENT)
	public static class ClientGameEvents {
		@SubscribeEvent
		public static void key(Key event) {
			KeyInput.tick();
		}
		@SubscribeEvent
		public static void mouseScrollingEvent(MouseScrollingEvent event) {
			MouseScroll.onMouseScroll(event);
		}
		@SubscribeEvent
		public static void renderLevelStageEvent(RenderLevelStageEvent event) {
			KineticParticle.tick(event);
			KineticDebugger.tick(event);
		}
	}
	@EventBusSubscriber(modid = ID, value = Dist.CLIENT, bus = Bus.MOD)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
			CCGKeyMapping.register(event);
		}
		@SubscribeEvent
		public static void registerGuiLayersEvent(RegisterGuiOverlaysEvent event) {
			OverlayRenderer.register(event);
		}
	}
}
