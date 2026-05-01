package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
public final class CCGConfig {
	@Category public final Goggles goggles = new Goggles();
	@Category public final Tooltip tooltip = new Tooltip();
	@Category public final GameMode gameMode = new GameMode();
	@Category public final Overlay overlay = new Overlay();
	@Category public final Outliner outliner = new Outliner();
	@Category public final ChainConveyor chainConveyor = new ChainConveyor();
	@Category public final Wrench wrench = new Wrench();
	@Category public final Aeronautics aeronautics = new Aeronautics();
	@Category public final Misc misc = new Misc();
	public static class Goggles {
		public boolean enhancedInfo = true;
		public boolean hideStaticKineticInfo = false;
		public boolean betterStoreInfo = true;
		public boolean enableKineticEffect = true;
		public boolean disableScreenGoggles = true;
		public boolean canRenderOnValueBox = false;
		public boolean dedupTooltipLines = true;
		public boolean preciseNumber = true;
		public int maxFractionDigits = 2;
	}
	public static class Tooltip {
		public boolean extraItemTooltip = true;
		public boolean container = true;
		public boolean fluidContainer = true;
		public boolean clipboard = true;
		public boolean map = true;
		public boolean toolbox = true;
		public boolean enderChest = true;
		public boolean goggles = true;
		public boolean backtank = true;
		public boolean divingBoots = true;
		public boolean wrench = true;
		public boolean linkedController = true;
		public boolean listFilter = true;
		public boolean attributeFilter = true;
		public boolean packageItem = true;
		public boolean packageEntity = true;
		public boolean itemEntity = true;
		public boolean deployer = true;
		public boolean depot = true;
		public boolean tableCloth = true;
		public boolean redstoneRequester = true;
		public boolean crushingController = true;
		public boolean millstone = true;
	}
	public static class GameMode {
		public boolean enableGoggles = true;
		public boolean enableInSurvival = true;
		public boolean enableInCreative = true;
		public boolean enableInSpectator = true;
		public boolean enableInAdventure = true;
	}
	public static class Overlay {
		public boolean renderItemOverlay = true;
		public int overlayOffsetX = 0;
		public int overlayOffsetY = 0;
		public TooltipFlagType tooltipFlagType = TooltipFlagType.Default;
		public TooltipTheme tooltipTheme = TooltipTheme.Default;
		public boolean useCustomColor = false;
		@ColorValue(hasAlpha = true) public int backgroundColor = 0x00000000;
		@ColorValue(hasAlpha = true) public int borderTopColor = 0x00000000;
		@ColorValue(hasAlpha = true) public int borderBottomColor = 0x00000000;
	}
	public static class Outliner {
		public boolean renderAnalogBox = true;
		public boolean betterLine = true;
		@IntRange(min = 0) public int delayRenderDuration = 60;
		@ColorValue public int outColor = 0xDDC166;
		@ColorValue public int inColor = 0x7FCDE0;
		public boolean rainbowDebug = false;
	}
	public static class ChainConveyor {
		public boolean alwaysAllowRiding = false;
		public boolean preventFalling = false;
		public boolean enhancedConnection = true;
		public boolean cardBoardedYourself = false;
	}
	public static class Wrench {
		public boolean betterEncasedCogwheel = true;
		public boolean betterEncasedPipe = true;
		public boolean betterChassis = true;
		public boolean alwaysShowScrollValue = true;
		public boolean alwaysAllowRotating = true;
		public boolean leftClickFastDismantle = true;
		public boolean removeCooldown = true;
		public boolean enchancedRotationMenu = false;
	}
	public static class Aeronautics {
		public boolean alwaysShowMass = true;
		public boolean alwaysShowFriction = false;
		public boolean liftLimitOfHandleRange = true;
	}
	public static class Misc {
		public boolean removeMechanicalArmLimit = false;
		public boolean removeRequestLimit = true;
		public boolean stockRequestQuickActions = true;
		public boolean recursiveSchematicScan = true;
		public boolean preventSelectionDiscard = true;
		public boolean infEditBoxLength = false;
		public boolean removeCardboardOverlay = true;
		public boolean removeNetheriteFirstPerson = false;
		public boolean allowDivingBoot = true;
		public boolean fixSchematicName = true;
		public boolean removeTrainDamage = false;
		public boolean enableNegativeInfThrottle = false;
		public boolean forcedBackend = false;
		public boolean nbtFix = false;
		public boolean showScrapContent = true;
	}
}
