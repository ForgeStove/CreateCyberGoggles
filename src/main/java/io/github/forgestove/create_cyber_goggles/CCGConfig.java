package io.github.forgestove.create_cyber_goggles;
import com.terraformersmc.modmenu.api.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.*;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.*;
import net.fabricmc.api.*;
@Config(name = CCG.ID)
public class CCGConfig implements ConfigData, ModMenuApi {
	@Category("goggles") @TransitiveObject public final Goggles goggles = new Goggles();
	@Category("gameMode") @TransitiveObject public final GameMode gameMode = new GameMode();
	@Category("delayRender") @TransitiveObject public final DelayRender delayRender = new DelayRender();
	@Category("misc") @TransitiveObject public final Misc misc = new Misc();
	@Override
	@Environment(EnvType.CLIENT)
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> AutoConfig.getConfigScreen(CCGConfig.class, screen).get();
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
		@Tooltip @ColorPicker public int airBoxPushColor = 0xDDC166;
		@Tooltip @ColorPicker public int airBoxPullColor = 0x7FCDE0;
	}
	public static class Misc {
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
