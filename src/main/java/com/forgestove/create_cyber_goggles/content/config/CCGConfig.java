package com.forgestove.create_cyber_goggles.content.config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
public class CCGConfig {
	public static CCGConfigData config;
	public static void register(ModContainer container) {
		config = AutoConfig.register(CCGConfigData.class, Toml4jConfigSerializer::new).getConfig();
		container.registerExtensionPoint(
			IConfigScreenFactory.class,
			(modContainer, screen) -> AutoConfig.getConfigScreen(CCGConfigData.class, screen).get()
		);
	}
	public static <T> void set(@NotNull Consumer<T> setter, T value) {
		setter.accept(value);
	}
}
