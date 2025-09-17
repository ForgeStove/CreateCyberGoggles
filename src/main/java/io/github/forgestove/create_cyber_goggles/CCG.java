package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.*;
public class CCG implements ClientModInitializer {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig CONFIG = AutoConfig.register(CCGConfig.class, Toml4jConfigSerializer::new).getConfig();
	@Override
	public void onInitializeClient() {
		CCGKeyMapping.register();
		ClientTickEvents.END_CLIENT_TICK.register(KeyInput::register);
		ClientTickEvents.END_CLIENT_TICK.register(DelayRender::tick);
		WorldRenderEvents.AFTER_ENTITIES.register(KineticParticle::tick);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(KineticDebugger::tick);
		HudRenderCallback.EVENT.register(OverlayRenderer::renderOverlay);
	}
}
