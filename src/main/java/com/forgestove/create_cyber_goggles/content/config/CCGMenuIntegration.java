package com.forgestove.create_cyber_goggles.content.config;
import com.terraformersmc.modmenu.api.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.*;
@Environment(EnvType.CLIENT)
public class CCGMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> AutoConfig.getConfigScreen(CCGConfig.class, screen).get();
	}
}
