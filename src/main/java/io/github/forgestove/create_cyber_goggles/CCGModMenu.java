package io.github.forgestove.create_cyber_goggles;
import com.terraformersmc.modmenu.api.*;
import io.github.forgestove.create_cyber_goggles.config.Config;
import net.fabricmc.api.*;
public class CCGModMenu implements ModMenuApi {
	@Override
	@Environment(EnvType.CLIENT)
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> Config.createConfigScreen(CCG.ID);
	}
}
