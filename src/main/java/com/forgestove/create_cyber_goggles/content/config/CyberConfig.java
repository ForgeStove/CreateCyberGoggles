package com.forgestove.create_cyber_goggles.content.config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
public class CyberConfig {
	static CyberConfigData config;
	public static void register(ModContainer container) {
		config = AutoConfig.register(CyberConfigData.class, Toml4jConfigSerializer::new).getConfig();
		container.registerExtensionPoint(
			IConfigScreenFactory.class,
			(modContainer, screen) -> AutoConfig.getConfigScreen(CyberConfigData.class, screen).get()
		);
	}
	public static CyberConfigData get() {
		if (config == null) config = AutoConfig.register(CyberConfigData.class, Toml4jConfigSerializer::new).getConfig();
		return config;
	}
}
