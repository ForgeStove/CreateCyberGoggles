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
	@Category("delayRender") @TransitiveObject public final DelayRender delayRender = new DelayRender();
	@Category("misc") @TransitiveObject public final Misc misc = new Misc();
	public static void register() {
		var factory = new ConfigScreenFactory((mc, screen) -> AutoConfig.getConfigScreen(CCGConfig.class, screen).get());
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> factory);
	}
	public static class Goggles {
		@Tooltip public boolean enhancedInfo = true;
		@Tooltip public boolean hideStaticKineticInfo = false;
		@Tooltip public boolean renderExtraItems = true;
		@Tooltip public boolean enableKineticEffect = true;
		@Tooltip public boolean preciseNumber = true;
		@Tooltip public boolean disableScreenGoggles = true;
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
		@Tooltip @ColorPicker public int windPushColor = 0xDDC166;
		@Tooltip @ColorPicker public int windPullColor = 0x7FCDE0;
	}
	public static class Misc {
		@Tooltip public boolean removeNetheriteFirstPerson = false;
		@Tooltip public boolean removeDivingFunction = false;
		@Tooltip public boolean fixSchematicName = true;
		@Tooltip public boolean rightClickPenetrate = false;
		@Tooltip public boolean rainbowDebug = false;
		@Tooltip public boolean forcedBackend = false;
		@Tooltip public boolean nbtFix = false;
		@Tooltip @RequiresRestart public boolean showScrapContent = true;
	}
}
