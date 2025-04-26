package com.forgestove.create_cyber_goggles.config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
public class Config {
	public static ModConfigData data;
	public static void register() {
		AutoConfig.register(ModConfigData.class, Toml4jConfigSerializer::new);
		ModLoadingContext.get().registerExtensionPoint(
				ConfigScreenFactory.class,
				() -> new ConfigScreenFactory((client, parent) -> AutoConfig.getConfigScreen(
						ModConfigData.class,
						parent
				).get())
		);
		data = AutoConfig.getConfigHolder(ModConfigData.class).getConfig();
	}
}
