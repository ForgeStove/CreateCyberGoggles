package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.content.ModConfig;
import com.forgestove.create_cyber_goggles.content.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
public class CreateCyberGoggles implements ClientModInitializer {
	public static final String ID = "create_cyber_goggles";
	public static ModConfig config;
	@Override
	public void onInitializeClient() {
		config = AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new).getConfig();
		KeyInput.register();
		ModKeyMapping.register();
		OverlayRenderer.register();
	}
}
