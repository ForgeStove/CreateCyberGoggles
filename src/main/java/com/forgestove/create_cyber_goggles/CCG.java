package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.content.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
public class CCG implements ClientModInitializer {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig CONFIG = AutoConfig.register(CCGConfig.class, Toml4jConfigSerializer::new).getConfig();
	@Override
	public void onInitializeClient() {
		CCGKeyMapping.register();
		KeyInput.register();
		OverlayRenderer.register();
		KineticParticle.register();
	}
}
