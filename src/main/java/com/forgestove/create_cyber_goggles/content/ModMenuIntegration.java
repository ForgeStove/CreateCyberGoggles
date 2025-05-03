package com.forgestove.create_cyber_goggles.content;
import com.terraformersmc.modmenu.api.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.*;
@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> AutoConfig.getConfigScreen(ModConfig.class, screen).get();
	}
}
