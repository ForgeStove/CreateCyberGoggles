package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
public class CCG implements ClientModInitializer {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig CONFIG = AutoConfig.register(CCGConfig.class, Toml4jConfigSerializer::new).getConfig();
	@SuppressWarnings("deprecation")
	@Override
	public void onInitializeClient() {
		CCGKey.register();
		ClientTickEvents.END_CLIENT_TICK.register(KeyInput::register);
		ClientTickEvents.END_CLIENT_TICK.register(DelayRender::tick);
		WorldRenderEvents.AFTER_ENTITIES.register(KineticDebugger::tick);
		WorldRenderEvents.AFTER_ENTITIES.register(KineticParticle::tick);
		HudRenderCallback.EVENT.register(OverlayRenderer::renderOverlay);
		var screen = Minecraft.getInstance().screen;
		if (screen != null) ScreenMouseEvents.allowMouseScroll(screen).register(MouseScroll::onMouseScroll);
	}
}
