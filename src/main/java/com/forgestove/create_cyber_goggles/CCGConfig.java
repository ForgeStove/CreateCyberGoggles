package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.CCGConfig.Goggles.GameMode;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
@Config(name = CCG.ID)
public class CCGConfig implements ConfigData {
	@Category("goggles") @TransitiveObject public Goggles goggles = new Goggles();
	@Category("goggles") @CollapsibleObject public GameMode gameMode = new GameMode();
	@Category("chainConveyor") @TransitiveObject public ChainConveyor chainConveyor = new ChainConveyor();
	@Category("armor") @TransitiveObject public Armor armor = new Armor();
	@Category("wrench") @TransitiveObject public Wrench wrench = new Wrench();
	@Category("other") @TransitiveObject public Other other = new Other();
	public static void register(ModContainer container) {
		container.registerExtensionPoint(
			IConfigScreenFactory.class,
			(modContainer, screen) -> AutoConfig.getConfigScreen(CCGConfig.class, screen).get()
		);
	}
	public static class Goggles {
		@Tooltip public boolean enhancedInfo = true;
		@Tooltip public boolean hideStaticKineticInfo = false;
		@Tooltip public boolean enhancedStoreRender = true;
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
	public static class ChainConveyor {
		@Tooltip public boolean alwaysAllowRiding = false;
		@Tooltip public boolean preventFalling = false;
		@Tooltip public boolean enhancedConnection = true;
		@Tooltip public boolean cardBoardedYourself = false;
	}
	public static class Armor {
		@Tooltip public boolean removeBoxOverlay = true;
		@Tooltip public boolean removeNetheriteFirstPerson = false;
		@Tooltip public boolean removeDivingBootsAffect = false;
	}
	public static class Wrench {
		@Tooltip public boolean alwaysAllowRotating = true;
		@Tooltip public boolean removeCooldown = true;
	}
	public static class Other {
		@Tooltip public boolean rightClickPenetrate = false;
		@Tooltip public boolean rainbowDebug = false;
		@Tooltip public boolean forcedBackend = false;
		@Tooltip @RequiresRestart public boolean nonrandomScrap = true;
		@Tooltip public boolean nbtFix = false;
	}
}
