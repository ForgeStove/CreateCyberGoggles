package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.*;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.InputEvent.*;
import net.neoforged.neoforge.client.event.*;
@Mod(CCG.ID)
public class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig CONFIG = AutoConfig.register(CCGConfig.class, Toml4jConfigSerializer::new).getConfig();
	public CCG(ModContainer container, Dist dist) {
		if (dist == Dist.CLIENT) CCGConfig.register(container);
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
		public static void registerGuiLayersEvent(RegisterGuiLayersEvent event) {
			OverlayRenderer.register(event);
		}
	}
}
