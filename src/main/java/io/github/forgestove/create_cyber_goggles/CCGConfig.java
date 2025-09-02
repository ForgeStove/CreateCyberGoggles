package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.CCGConfig.Goggles.GameMode;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
@Config(name = CCG.ID)
public class CCGConfig implements ConfigData {
	@Category("goggles") @TransitiveObject public final Goggles goggles = new Goggles();
	@Category("goggles") @CollapsibleObject public final GameMode gameMode = new GameMode();
	@Category("chainConveyor") @TransitiveObject public final ChainConveyor chainConveyor = new ChainConveyor();
	@Category("armor") @TransitiveObject public final Armor armor = new Armor();
	@Category("wrench") @TransitiveObject public final Wrench wrench = new Wrench();
	@Category("other") @TransitiveObject public final Other other = new Other();
	public static void register() {
		var factory = new ConfigScreenFactory((mc, screen) -> AutoConfig.getConfigScreen(CCGConfig.class, screen).get());
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> factory);
	}
	public static class Goggles {
		@Tooltip public boolean enhancedInfo = true;
		@Tooltip public boolean hideStaticKineticInfo = false;
		@Tooltip public boolean betterStoreInfo = true;
		@Tooltip public boolean renderExtraItems = true;
		@Tooltip public boolean enableKineticEffect = true;
		@Tooltip public boolean preciseNumbers = true;
		@Tooltip public boolean disableScreenGoggles = true;
		@Tooltip public boolean betterLine = true;
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
		@Tooltip @RequiresRestart public boolean showScrapContent = true;
		@Tooltip public boolean nbtFix = false;
	}
}
