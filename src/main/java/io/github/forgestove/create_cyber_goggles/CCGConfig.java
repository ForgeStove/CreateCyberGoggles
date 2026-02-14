package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.config.ConfigHandler;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
public class CCGConfig {
	private static final String TRANSLATION_PREFIX = CCG.ID + ".config";
	public static final ConfigHandler<CCGConfig> CONFIG_HANDLER = ConfigHandler.builder(CCGConfig.class)
		.path(() -> FMLPaths.CONFIGDIR.get().resolve(CCG.ID + ".toml"))
		.translationPrefix(TRANSLATION_PREFIX)
		.translator(key -> I18n.exists(key) ? I18n.get(key) : null)
		.logger(CCG.LOGGER)
		.build();
	@ConfigCategory(ordinal = 1) public final Goggles goggles = new Goggles();
	@ConfigCategory(ordinal = 2) public final GameMode gameMode = new GameMode();
	@ConfigCategory(ordinal = 3) public final Overlay overlay = new Overlay();
	@ConfigCategory(ordinal = 4) public final Outliner outliner = new Outliner();
	@ConfigCategory(ordinal = 5) public final ChainConveyor chainConveyor = new ChainConveyor();
	@ConfigCategory(ordinal = 6) public final Wrench wrench = new Wrench();
	@ConfigCategory(ordinal = 7) public final Misc misc = new Misc();
	public static void init() {
		CCG.config = CONFIG_HANDLER.getConfig();
		ModLoadingContext.get()
			.registerExtensionPoint(
				ConfigScreenFactory.class,
				() -> new ConfigScreenFactory((client, parent) -> CONFIG_HANDLER.createConfigScreen(parent))
			);
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
		public TooltipTheme tooltipTheme = TooltipTheme.Default;
		public boolean useCustomColor = false;
		@ColorValue(hasAlpha = true) public int backgroundColor = 0x00000000;
		@ColorValue(hasAlpha = true) public int borderTopColor = 0x00000000;
		@ColorValue(hasAlpha = true) public int borderBottomColor = 0x00000000;
	}
	public static class Outliner {
		public boolean renderAnalogBox = true;
		public boolean betterLine = true;
		@Range(min = 0) public int delayRenderDuration = 60;
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
		public boolean fixRotationMenu = true;
		public boolean betterEncasedCogwheel = true;
		public boolean betterEncasedPipe = true;
		public boolean betterChassis = true;
		public boolean alwaysShowScrollValue = true;
		public boolean alwaysAllowRotating = true;
		public boolean leftClickFastDismantle = true;
		public boolean removeCooldown = true;
		public boolean enchancedRotationMenu = false;
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
		@RequiresRestart public boolean showScrapContent = true;
	}
}
