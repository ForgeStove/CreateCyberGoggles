package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.*;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
@Config(name = CCG.ID)
public class CCGConfig implements ConfigData {
	@Category("goggles") @TransitiveObject public final Goggles goggles = new Goggles();
	@Category("gameMode") @TransitiveObject public final GameMode gameMode = new GameMode();
	@Category("overlay") @TransitiveObject public final Overlay overlay = new Overlay();
	@Category("outliner") @TransitiveObject public final Outliner outliner = new Outliner();
	@Category("chainConveyor") @TransitiveObject public final ChainConveyor chainConveyor = new ChainConveyor();
	@Category("wrench") @TransitiveObject public final Wrench wrench = new Wrench();
	@Category("misc") @TransitiveObject public final Misc misc = new Misc();
	public static void register() {
		var factory = new ConfigScreenFactory((mc, screen) -> AutoConfig.getConfigScreen(CCGConfig.class, screen).get());
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> factory);
	}
	public static class Goggles {
		@Tooltip public boolean enhancedInfo = true;
		@Tooltip public boolean hideStaticKineticInfo = false;
		@Tooltip public boolean betterStoreInfo = true;
		@Tooltip public boolean enableKineticEffect = true;
		@Tooltip public boolean preciseNumber = true;
		@Tooltip public boolean disableScreenGoggles = true;
		@Tooltip public boolean canRenderOnValueBox = false;
	}
	public static class GameMode {
		@Tooltip public boolean enableGoggles = true;
		@Tooltip public boolean enableInSurvival = true;
		@Tooltip public boolean enableInCreative = true;
		@Tooltip public boolean enableInSpectator = true;
		@Tooltip public boolean enableInAdventure = true;
	}
	public static class Overlay {
		@Tooltip public boolean renderExtraItems = true;
		@Tooltip public int overlayOffsetX = 0;
		@Tooltip public int overlayOffsetY = 0;
		@EnumHandler(option = EnumDisplayOption.BUTTON) public TooltipFlagType tooltipFlagType = TooltipFlagType.Default;
		@EnumHandler(option = EnumDisplayOption.BUTTON) public TooltipTheme tooltipTheme = TooltipTheme.Default;
		@Tooltip public boolean useCustomColor = false;
		@Tooltip @ColorPicker(allowAlpha = true) public int backgroundColor = 0x00000000;
		@Tooltip @ColorPicker(allowAlpha = true) public int borderTopColor = 0x00000000;
		@Tooltip @ColorPicker(allowAlpha = true) public int borderBottomColor = 0x00000000;
	}
	public static class Outliner {
		@Tooltip public boolean renderAnalogBox = true;
		@Tooltip public boolean betterLine = true;
		@Tooltip public int delayRenderDuration = 60;
		@Tooltip @ColorPicker public int outColor = 0xDDC166;
		@Tooltip @ColorPicker public int inColor = 0x7FCDE0;
		@Tooltip public boolean rainbowDebug = false;
	}
	public static class ChainConveyor {
		@Tooltip public boolean alwaysAllowRiding = false;
		@Tooltip public boolean preventFalling = false;
		@Tooltip public boolean enhancedConnection = true;
		@Tooltip public boolean cardBoardedYourself = false;
	}
	public static class Wrench {
		@Tooltip public boolean fixRotationMenu = true;
		@Tooltip public boolean betterEncasedCogwheel = true;
		@Tooltip public boolean betterEncasedPipe = true;
		@Tooltip public boolean betterChassis = true;
		@Tooltip public boolean alwaysShowScrollValue = true;
		@Tooltip public boolean alwaysAllowRotating = true;
		@Tooltip public boolean leftClickFastDismantle = true;
		@Tooltip public boolean removeCooldown = true;
		@Tooltip public boolean enchancedRotationMenu = false;
	}
	public static class Misc {
		@Tooltip public boolean preventSelectionDiscard = true;
		@Tooltip public boolean infEditBoxLength = false;
		@Tooltip public boolean removeCardboardOverlay = true;
		@Tooltip public boolean removeNetheriteFirstPerson = false;
		@Tooltip public boolean allowDivingBoot = true;
		@Tooltip public boolean fixSchematicName = true;
		@Tooltip public boolean forcedBackend = false;
		@Tooltip public boolean nbtFix = false;
		@Tooltip @RequiresRestart public boolean showScrapContent = true;
	}
}
