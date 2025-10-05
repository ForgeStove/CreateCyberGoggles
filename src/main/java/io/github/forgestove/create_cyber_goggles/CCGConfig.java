package io.github.forgestove.create_cyber_goggles;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.*;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
@Config(name = CCG.ID)
public class CCGConfig implements ConfigData {
	@Category("goggles") @TransitiveObject public final Goggles goggles = new Goggles();
	@Category("gameMode") @TransitiveObject public final GameMode gameMode = new GameMode();
	@Category("outlineRenderer") @TransitiveObject public final OutlineRenderer outlineRenderer = new OutlineRenderer();
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
		@Tooltip public boolean renderExtraItems = true;
		@Tooltip public boolean enableKineticEffect = true;
		@Tooltip public boolean preciseNumber = true;
		@Tooltip public boolean disableScreenGoggles = true;
		@Tooltip public boolean canRenderOnValueBox = false;
		@Tooltip public boolean betterLine = true;
	}
	public static class GameMode {
		@Tooltip public boolean enableGoggle = true;
		@Tooltip public boolean enableInSurvival = true;
		@Tooltip public boolean enableInCreative = true;
		@Tooltip public boolean enableInSpectator = true;
		@Tooltip public boolean enableInAdventure = true;
	}
	public static class OutlineRenderer {
		@Tooltip public boolean renderAnalogBox = true;
		@Tooltip public int delayRenderDuration = 60;
		@Tooltip @ColorPicker public int windPushColor = 0xDDC166;
		@Tooltip @ColorPicker public int windPullColor = 0x7FCDE0;
		@Tooltip public boolean rainbowDebug = false;
	}
	public static class ChainConveyor {
		@Tooltip public boolean alwaysAllowRiding = false;
		@Tooltip public boolean preventFalling = false;
		@Tooltip public boolean enhancedConnection = true;
		@Tooltip public boolean cardBoardedYourself = false;
	}
	public static class Wrench {
		@Tooltip @RequiresRestart public boolean enchancedWrench = true;
		@Tooltip public boolean betterEncasedPipe = true;
		@Tooltip public boolean alwaysAllowRotating = true;
		@Tooltip public boolean leftClickFastDismantle = true;
		@Tooltip public boolean removeCooldown = true;
	}
	public static class Misc {
		@Tooltip public boolean infEditBoxLength = false;
		@Tooltip public boolean removeCardboardOverlay = true;
		@Tooltip public boolean removeNetheriteFirstPerson = false;
		@Tooltip public boolean allowDivingBoot = true;
		@Tooltip public boolean fixSchematicName = true;
		@Tooltip public boolean rightClickPenetrate = false;
		@Tooltip public boolean forcedBackend = false;
		@Tooltip public boolean nbtFix = false;
		@Tooltip @RequiresRestart public boolean showScrapContent = true;
	}
}
