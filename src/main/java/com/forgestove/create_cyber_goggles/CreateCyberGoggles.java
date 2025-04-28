package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.config.ModConfig;
import com.forgestove.create_cyber_goggles.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
public class CreateCyberGoggles implements ClientModInitializer {
	public static final String ID = "create_cyber_goggles";
	public static ModConfig config = AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new).getConfig();
	@Override
	public void onInitializeClient() {
		KeyInput.register();
		ModKeyMapping.register();
		OverlayRenderer.register();
	}
}
