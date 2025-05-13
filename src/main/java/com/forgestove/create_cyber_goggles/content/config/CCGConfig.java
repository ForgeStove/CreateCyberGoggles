package com.forgestove.create_cyber_goggles.content.config;
import com.forgestove.create_cyber_goggles.content.util.SafeRun;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
public class CCGConfig {
	static CCGConfigData config;
	public static void register(ModContainer container) {
		SafeRun.run(() -> config = AutoConfig.register(CCGConfigData.class, Toml4jConfigSerializer::new).getConfig());
		container.registerExtensionPoint(
			IConfigScreenFactory.class,
			(modContainer, screen) -> AutoConfig.getConfigScreen(CCGConfigData.class, screen).get()
		);
	}
	public static CCGConfigData get() {
		return config != null ? config : (config = AutoConfig.register(CCGConfigData.class, Toml4jConfigSerializer::new).getConfig());
	}
}
