package io.github.forgestove.create_cyber_goggles;
import com.terraformersmc.modmenu.api.*;
import io.github.forgestove.create_cyber_goggles.config.Config;
import org.jetbrains.annotations.*;
public class CCGModMenuImpl implements ModMenuApi {
	@Contract(pure = true)
	@Override
	public @NotNull ConfigScreenFactory<?> getModConfigScreenFactory() {
		return Config.createModMenuFactory(CCG.ID);
	}
}
