package com.forgestove.create_cyber_goggles.config;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
@Config(name = CreateCyberGoggles.ID)
public class ModConfig implements ConfigData {
	@Category("armor") @TransitiveObject public Armor armor = new Armor();
	@Category("chainConveyor") @TransitiveObject public ChainConveyor chainConveyor = new ChainConveyor();
	@Category("flywheel") @TransitiveObject public Flywheel flywheel = new Flywheel();
	@Category("goggles") @TransitiveObject public Goggles goggles = new Goggles();
	@Category("nbt") @TransitiveObject public Nbt nbt = new Nbt();
	@Category("wrench") @TransitiveObject public Wrench wrench = new Wrench();
	public static class Armor {
		@Tooltip public boolean removeBoxOverlay = true;
		@Tooltip public boolean removeNetheriteFirstPerson = false;
		@Tooltip public boolean removeDivingBootsAffect = false;
	}
	public static class ChainConveyor {
		@Tooltip public boolean alwaysAllowRiding = false;
		@Tooltip public boolean preventFalling = false;
		@Tooltip public int separationDistance = 3;
		@Tooltip public int separationHeight = -1;
		@Tooltip public boolean enhancedConnection = true;
		@Tooltip public boolean cardBoardedYourself = false;
	}
	public static class Flywheel {
		@Tooltip public boolean forcedBackend = false;
	}
	public static class Goggles {
		@Tooltip public boolean enableInSurvival = true;
		@Tooltip public boolean enableInCreative = true;
		@Tooltip public boolean enableInSpectator = true;
		@Tooltip public boolean enableInAdventure = true;
		@Tooltip public boolean enhancedInfo = true;
		@Tooltip public boolean enhancedStoreRender = true;
		@Tooltip public boolean renderExtraItems = true;
		@Tooltip public boolean enableKineticEffect = true;
		@Tooltip public boolean preciseNumbers = true;
	}
	public static class Nbt {
		@Tooltip public boolean nbtFix = false;
	}
	public static class Wrench {
		@Tooltip public boolean alwaysAllowRotating = true;
	}
}
