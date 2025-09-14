package io.github.forgestove.create_cyber_goggles;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.*;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;
@Config(name = CCG.ID)
public class CCGConfig implements ConfigData {
	@Category("goggles") @TransitiveObject public final Goggles goggles = new Goggles();
	@Category("gameMode") @TransitiveObject public final GameMode gameMode = new GameMode();
	@Category("delayRender") @TransitiveObject public final DelayRender delayRender = new DelayRender();
	@Category("chainConveyor") @TransitiveObject public final ChainConveyor chainConveyor = new ChainConveyor();
	@Category("wrench") @TransitiveObject public final Wrench wrench = new Wrench();
	@Category("other") @TransitiveObject public final Other other = new Other();
	public static void register(@NotNull ModContainer container) {
		IConfigScreenFactory factory = (modContainer, screen) -> AutoConfig.getConfigScreen(CCGConfig.class, screen).get();
		container.registerExtensionPoint(IConfigScreenFactory.class, factory);
	}
	public static class Goggles {
		@Tooltip public boolean enhancedInfo = true;
		@Tooltip public boolean hideStaticKineticInfo = false;
		@Tooltip public boolean betterStoreInfo = true;
		@Tooltip public boolean renderExtraItems = true;
		@Tooltip public boolean enableKineticEffect = true;
		@Tooltip public boolean preciseNumber = true;
		@Tooltip public boolean disableScreenGoggles = true;
		@Tooltip public boolean betterLine = true;
	}
	public static class GameMode {
		@Tooltip public boolean enableInSurvival = true;
		@Tooltip public boolean enableInCreative = true;
		@Tooltip public boolean enableInSpectator = true;
		@Tooltip public boolean enableInAdventure = true;
	}
	public static class DelayRender {
		@Tooltip public boolean renderAnalogBox = true;
		@Tooltip public int delayRenderDuration = 60;
		@Tooltip @ColorPicker public int airBoxPushColor = 0xDDC166;
		@Tooltip @ColorPicker public int airBoxPullColor = 0x7FCDE0;
	}
	public static class ChainConveyor {
		@Tooltip public boolean alwaysAllowRiding = false;
		@Tooltip public boolean preventFalling = false;
		@Tooltip public boolean enhancedConnection = true;
		@Tooltip public boolean cardBoardedYourself = false;
	}
	public static class Wrench {
		@Tooltip public boolean alwaysAllowRotating = true;
		@Tooltip public boolean removeCooldown = true;
	}
	public static class Other {
		@Tooltip public boolean removeCardboardOverlay = true;
		@Tooltip public boolean removeNetheriteFirstPerson = false;
		@Tooltip public boolean removeDivingBootsAffect = false;
		@Tooltip public boolean fixSchematicName = true;
		@Tooltip public boolean rightClickPenetrate = false;
		@Tooltip public boolean rainbowDebug = false;
		@Tooltip public boolean forcedBackend = false;
		@Tooltip public boolean nbtFix = false;
		@Tooltip @RequiresRestart public boolean showScrapContent = true;
	}
}
