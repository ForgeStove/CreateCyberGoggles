package com.forgestove.create_cyber_goggles.content.config;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
@Config(name = CreateCyberGoggles.ID)
public class CCGConfigData implements ConfigData {
	@Category("goggles") @TransitiveObject public Goggles goggles = new Goggles();
	@Category("armor") @TransitiveObject public Armor armor = new Armor();
	@Category("flywheel") @TransitiveObject public Flywheel flywheel = new Flywheel();
	@Category("jei") @TransitiveObject public Jei jei = new Jei();
	@Category("nbt") @TransitiveObject public Nbt nbt = new Nbt();
	public static class Goggles {
		@Tooltip public boolean enhancedInfo = true;
		@Tooltip public boolean hideStaticKineticInfo = false;
		@Tooltip public boolean renderExtraItems = true;
		@Tooltip public boolean enableKineticEffect = true;
		@Tooltip public boolean preciseNumbers = true;
		@Tooltip public boolean enableInSurvival = true;
		@Tooltip public boolean enableInCreative = true;
		@Tooltip public boolean enableInSpectator = true;
		@Tooltip public boolean enableInAdventure = true;
	}
	public static class Armor {
		@Tooltip public boolean removeNetheriteFirstPerson = false;
		@Tooltip public boolean removeDivingBootsAffect = false;
	}
	public static class Flywheel {
		@Tooltip public boolean forcedBackend = false;
	}
	public static class Jei {
		@Tooltip @RequiresRestart public boolean nonrandomScrap = true;
	}
	public static class Nbt {
		@Tooltip public boolean nbtFix = false;
	}
}
