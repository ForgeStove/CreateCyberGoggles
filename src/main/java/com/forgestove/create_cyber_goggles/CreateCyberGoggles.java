package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.content.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
@Mod(CreateCyberGoggles.ID)
public class CreateCyberGoggles {
	public static final String ID = "create_cyber_goggles";
	public static ModConfig config;
	public CreateCyberGoggles() {
		config = AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new).getConfig();
		var factory = new ConfigScreenFactory((mc, screen) -> AutoConfig.getConfigScreen(ModConfig.class, screen).get());
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> factory);
	}
}
