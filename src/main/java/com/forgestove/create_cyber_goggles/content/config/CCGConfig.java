package com.forgestove.create_cyber_goggles.content.config;
import com.terraformersmc.modmenu.api.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
@Environment(EnvType.CLIENT)
public class CCGConfig implements ModMenuApi {
	public static CCGConfigData config;
	public static void register() {
		config = AutoConfig.register(CCGConfigData.class, Toml4jConfigSerializer::new).getConfig();
	}
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> AutoConfig.getConfigScreen(CCGConfigData.class, screen).get();
	}
	public static <T> void set(@NotNull Consumer<T> setter, T value) {
		setter.accept(value);
	}
}
