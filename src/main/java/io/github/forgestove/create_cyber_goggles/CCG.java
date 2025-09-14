package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.loading.FMLEnvironment;
@Mod(CCG.ID)
public class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig CONFIG = AutoConfig.register(CCGConfig.class, Toml4jConfigSerializer::new).getConfig();
	public CCG() {
		if (FMLEnvironment.dist == Dist.CLIENT) CCGConfig.register();
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
		}
		@SubscribeEvent
		public static void clientTickEvent(ClientTickEvent event) {
			KineticDebugger.tick();
			DelayRender.tick();
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
