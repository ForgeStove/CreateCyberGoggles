package com.forgestove.create_cyber_goggles.content.config;
import com.forgestove.create_cyber_goggles.content.util.*;
import com.terraformersmc.modmenu.api.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.*;
@Environment(EnvType.CLIENT)
public class CCGConfig implements ModMenuApi {
	private static CCGConfigData config;
	public static CCGConfigData get() {
		if (config == null) SafeRun.run(() -> config =
			AutoConfig.register(CCGConfigData.class, Toml4jConfigSerializer::new).getConfig());
		return config;
	}
	public static void register() {
		SafeRun.run(() -> config = AutoConfig.register(CCGConfigData.class, Toml4jConfigSerializer::new).getConfig());
	}
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> AutoConfig.getConfigScreen(CCGConfigData.class, screen).get();
	}
}
