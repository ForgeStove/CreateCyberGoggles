package com.forgestove.create_cyber_goggles.content.config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
public class CCGConfig {
	public static CCGConfigData config;
	public static void register() {
		config = AutoConfig.register(CCGConfigData.class, Toml4jConfigSerializer::new).getConfig();
		var factory = new ConfigScreenFactory((mc, screen) -> AutoConfig.getConfigScreen(CCGConfigData.class, screen).get());
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> factory);
	}
}
