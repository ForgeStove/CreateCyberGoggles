package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.core.config.SoundReloadHandler;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.flexconfig.api.*;

import java.awt.Point;
@Config(CCG.ID)
public final class CCGConfig {
	@Category public Goggles goggles = new Goggles();
	@Category public Tooltip tooltip = new Tooltip();
	@Category public Overlay overlay = new Overlay();
	@Category public Outliner outliner = new Outliner();
	@Category public Schematic schematic = new Schematic();
	@Category @Condition("simulated") public Aeronautics aeronautics = new Aeronautics();
	@Category public Misc misc = new Misc();
	public static class Goggles {
		@Category(false) public GameMode gameMode = new GameMode();
		public boolean enhancedInfo = true;
		public boolean hideStaticKineticInfo = false;
		public boolean onlyOnWithGoggles = true;
		public boolean betterStoreInfo = true;
		public boolean betterFactoryGauge = true;
		public boolean enableKineticEffect = true;
		public boolean disableInScreenGoggles = true;
		public boolean canRenderOnValueBox = false;
		public boolean enableFadeOut = true;
		public boolean preciseNumber = true;
		@IntRange(min = 0) public int maxFractionDigits = 2;
		public static class GameMode {
			public boolean enableGoggles = true;
			public boolean enableInSurvival = true;
			public boolean enableInCreative = true;
			public boolean enableInSpectator = true;
			public boolean enableInAdventure = true;
		}
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
		public boolean placard = true;
		public boolean itemEntity = true;
		public boolean deployer = true;
		public boolean depot = true;
		public boolean tableCloth = true;
		public boolean redstoneRequester = true;
		public boolean crushingController = true;
		public boolean millstone = true;
	}
	public static class Overlay {
		public boolean renderItemOverlay = true;
		public Point overlayPos = new Point();
		public TooltipFlagType tooltipFlagType = TooltipFlagType.Default;
		public TooltipTheme tooltipTheme = TooltipTheme.Default;
		public boolean useCustomColor = false;
		@ColorValue(true) public int backgroundColor = 0x00000000;
		@ColorValue(true) public int borderTopColor = 0x00000000;
		@ColorValue(true) public int borderBottomColor = 0x00000000;
		@Category public DraftingView draftingView = new DraftingView();
		public static class DraftingView {
			public boolean draftingViewEnabled = false;
			@DoubleRange(min = 0, max = 1) public double paletteOffset = 0.25;
			public boolean pixelate = true;
			@DoubleRange(min = 1, max = 16) public double pixelScale = 4;
			@ColorValue public int lineColor = 0x2E3032;
			@ColorValue public int lineShadowColor = 0x696965;
		}
	}
	public static class Outliner {
		public boolean renderAnalogBox = true;
		public boolean betterLine = true;
		@IntRange(min = 0) public int delayRenderDuration = 60;
		@ColorValue public int outColor = 0xDDC166;
		@ColorValue public int inColor = 0x7FCDE0;
		public boolean rainbowDebug = false;
	}
	public static class Schematic {
		@Category public Preview preview = new Preview();
		@Category public Image image = new Image();
		public boolean truncateSchematicName = true;
		public static class Preview {
			public boolean previewEnabled = true;
			public boolean tooltipPreview = true;
			@IntRange(min = 1) public int maxBlockVolume = 216000;
			@IntRange(min = 48, max = 512) public int sidePanelWidth = 156;
			@IntRange(min = 48, max = 512) public int maxHeight = 156;
			@IntRange(min = 0, max = 5000) public int loadDelayMs = 0;
			@FloatRange(min = 0.5F, max = 3F) public float defaultZoom = 1.25F;
			@FloatRange(min = -180F, max = 180F) public float defaultYaw = 45F;
			@FloatRange(min = -90F, max = 90F) public float defaultPitch = 30F;
		}
		public static class Image {
			@IntRange(min = 64, max = 8192) public int defaultWidth = 1024;
			@IntRange(min = 64, max = 8192) public int alternateWidth = 2048;
			@IntRange(min = 1, max = 4) public int defaultAntialiasing = 2;
			public boolean usePreviewRotation = true;
		}
	}
	public static class Aeronautics {
		public boolean navigationDistance = true;
		public boolean alwaysShowMass = true;
		public boolean alwaysShowFriction = false;
		public boolean liftLimitOfHandleRange = false;
		public boolean customHandleMoveSublevelKey = false;
		@WarnCheat public boolean alwaysAllowRidingRope = false;
		@WarnCheat public boolean enablePhysicsStaff = false;
		@Category public ForceOverlay forceOverlay = new ForceOverlay();
		public static class ForceOverlay {
			public boolean enableForceOverlay = true;
			public boolean hudPanelEnabled = true;
			public Point forceOverlayPos = new Point();
			public boolean useWorldLabels = true;
			@DoubleRange(min = 0, max = 10) public double worldLabelScale = 1.0;
			@DoubleRange(min = 0) public double clusterAngleRadians = 0.1;
			@DoubleRange(min = 0, max = 1) public double smoothingFactor = 0.5;
			@DoubleRange(min = 0, max = 1) public double gravityArrowFraction = 0.5;
			@DoubleRange(min = 1) public double arrowSaturation = 3;
			@DoubleRange(min = 0) public double minArrowLength = 0.1;
			@IntRange(min = 1, max = 64) public int targetingChunks = 4;
			@IntRange(min = 1) public int heartbeatIntervalTicks = 10;
			@DoubleRange(min = 0) public double minOverlayPixelSize = 0;
			public boolean showCenterOfMass = true;
			public boolean showGravity = true;
			public boolean showDrag = true;
			public boolean showLevitation = true;
			public boolean showBalloonLift = true;
			public boolean showPropulsion = true;
			public boolean showLift = true;
			public boolean showMagneticForce = true;
		}
	}
	public static class Misc {
		@Category(false) public ChainConveyor chainConveyor = new ChainConveyor();
		@Category(false) public Wrench wrench = new Wrench();
		@Category public CreateStackCount createStackCount = new CreateStackCount();
		@Category public Jei jei = new Jei();
		public boolean quickRequestActions = true;
		public boolean removeRequestLimit = true;
		@WarnCheat public boolean removeMechanicalArmLimit = false;
		@WarnCheat public boolean removePackagePortDownLimit = false;
		public boolean infEditBoxLength = false;
		public boolean removeCardboardOverlay = true;
		public boolean removeNetheriteFirstPerson = false;
		public boolean preventSelectionDiscard = true;
		public boolean preventAutoCloseFilter = false;
		public boolean allowDivingBoot = true;
		public boolean recursiveSchematicScan = true;
		public boolean fixSchematicName = true;
		public boolean autoReplenishStock = true;
		@WarnCheat public boolean removeTrainDamage = false;
		@WarnCheat public boolean enableNegativeInfThrottle = false;
		public boolean forcedBackend = false;
		public boolean nbtFix = false;
		@OnChange(SoundReloadHandler.class) @IntRange(min = 255, max = 8192) public int soundPoolLimit = 255;
		public static class ChainConveyor {
			@WarnCheat public boolean alwaysAllowRidingChain = false;
			public boolean preventFalling = false;
			@WarnCheat public boolean enhancedConnection = true;
			public boolean cardBoardedYourself = false;
		}
		public static class Wrench {
			public boolean betterEncasedCogwheel = true;
			public boolean betterEncasedPipe = true;
			public boolean betterChassis = true;
			public boolean alwaysShowScrollValue = true;
			public boolean leftClickFastDismantle = true;
			public boolean removeCooldown = true;
			@WarnCheat public boolean alwaysAllowRotating = true;
			@WarnCheat public boolean enchancedRotationMenu = false;
		}
		public static class CreateStackCount {
			public boolean enableCreateStyleStackCount = false;
			@ColorValue(true) public int countOutlineColor = 0xFF383838;
		}
		public static class Jei {
			public boolean allowLargeCrafting = true;
			public boolean optimizeRecipeProcessing = true;
			public boolean redstoneRequesterJEIRequest = true;
			public boolean showScrapContent = true;
		}
	}
}
