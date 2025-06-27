package io.github.forgestove.create_cyber_goggles;
import com.terraformersmc.modmenu.api.*;
import io.github.forgestove.create_cyber_goggles.CCGConfig.Goggles.GameMode;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
import net.fabricmc.api.*;
@Config(name = CCG.ID)
public class CCGConfig implements ConfigData, ModMenuApi {
	@Category("goggles") @TransitiveObject public Goggles goggles = new Goggles();
	@Category("goggles") @CollapsibleObject public GameMode gameMode = new GameMode();
	@Category("armor") @TransitiveObject public Armor armor = new Armor();
	@Category("other") @TransitiveObject public Other other = new Other();
	@Override
	@Environment(EnvType.CLIENT)
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> AutoConfig.getConfigScreen(CCGConfig.class, screen).get();
	}
	public static class Goggles {
		@Tooltip public boolean enhancedInfo = true;
		@Tooltip public boolean hideStaticKineticInfo = false;
		@Tooltip public boolean renderExtraItems = true;
		@Tooltip public boolean enableKineticEffect = true;
		@Tooltip public boolean preciseNumbers = true;
		public static class GameMode {
			@Tooltip public boolean enableInSurvival = true;
			@Tooltip public boolean enableInCreative = true;
			@Tooltip public boolean enableInSpectator = true;
			@Tooltip public boolean enableInAdventure = true;
		}
	}
	public static class Armor {
		@Tooltip public boolean removeNetheriteFirstPerson = false;
		@Tooltip public boolean removeDivingBootsAffect = false;
	}
	public static class Other {
		@Tooltip public boolean forcedBackend = false;
		@Tooltip @RequiresRestart public boolean nonrandomScrap = true;
		@Tooltip public boolean nbtFix = false;
	}
}
