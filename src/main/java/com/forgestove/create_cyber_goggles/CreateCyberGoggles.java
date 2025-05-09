package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.event.*;
import net.fabricmc.api.ClientModInitializer;
public class CreateCyberGoggles implements ClientModInitializer {
	public static final String ID = "create_cyber_goggles";
	@Override
	public void onInitializeClient() {
		CCGConfig.register();
		CCGKeyMapping.register();
		KeyInput.register();
		OverlayRenderer.register();
	}
}
