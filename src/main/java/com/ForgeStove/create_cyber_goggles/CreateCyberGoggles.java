package com.ForgeStove.create_cyber_goggles;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
@Mod(CreateCyberGoggles.MOD_ID) public class CreateCyberGoggles {
	public static final String MOD_ID = "create_cyber_goggles";
	public CreateCyberGoggles(ModContainer modContainer) {
		modContainer.registerConfig(Type.CLIENT, Config.CONFIG_SPEC);
	}
}
