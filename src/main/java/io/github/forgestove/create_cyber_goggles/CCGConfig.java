package io.github.forgestove.create_cyber_goggles;
import com.terraformersmc.modmenu.api.*;
import io.github.forgestove.create_cyber_goggles.config.Config;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.core.factory.TooltipFlagType;
import net.fabricmc.api.*;
@ConfigClass(CCG.ID)
public class CCGConfig implements ModMenuApi {
	@Category public final Goggles goggles = new Goggles();
	@Category public final GameMode gameMode = new GameMode();
	@Category public final Overlay overlay = new Overlay();
	@Category public final Outliner outliner = new Outliner();
	@Category public final ChainConveyor chainConveyor = new ChainConveyor();
	@Category public final Wrench wrench = new Wrench();
	@Category public final Tooltip tooltip = new Tooltip();
	@Category public final Misc misc = new Misc();
	@Override
	@Environment(EnvType.CLIENT)
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> Config.createConfigScreen(CCG.ID);
	}
	public static class Goggles {
		public boolean enhancedInfo = true;
		public boolean hideStaticKineticInfo = false;
		public boolean betterStoreInfo = true;
		public boolean enableKineticEffect = true;
		public boolean preciseNumber = true;
		public boolean disableScreenGoggles = true;
		public boolean canRenderOnValueBox = false;
		public boolean extraItemTooltip = true;
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
	}
	public static class Outliner {
		public boolean renderAnalogBox = true;
		public boolean betterLine = true;
		public int delayRenderDuration = 60;
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
	public static class Tooltip {
		public boolean clipboard = true;
		public boolean container = true;
		public boolean enderChest = true;
		public boolean linkedController = true;
		public boolean listFilter = true;
		public boolean map = true;
		public boolean packageItem = true;
		public boolean redstoneRequester = true;
		public boolean tableCloth = true;
		public boolean toolbox = true;
	}
	public static class Misc {
		public boolean removeMechanicalArmLimit = false;
		public boolean removeRequestLimit = true;
		public boolean preventSelectionDiscard = true;
		public boolean infEditBoxLength = false;
		public boolean removeCardboardOverlay = true;
		public boolean removeNetheriteFirstPerson = false;
		public boolean allowDivingBoot = true;
		public boolean fixSchematicName = true;
		public boolean forcedBackend = false;
		public boolean nbtFix = false;
	}
}
