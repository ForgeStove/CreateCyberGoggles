package com.ForgeStove.create_cyber_goggles;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;
public class Config {
	public static final Builder BUILDER = new Builder();
	public static final ModConfigSpec CLIENT_SPEC;
	public static final ConfigValue<Boolean> alwaysAllowRiding;
	public static final ConfigValue<Boolean> preventFalling;
	public static final ConfigValue<Integer> separationDistance;
	public static final ConfigValue<Integer> separationHeight;
	public static final ConfigValue<Boolean> enhancedConnection;
	public static final ConfigValue<Boolean> forcedBackend;
	public static final ConfigValue<Boolean> enableInSurvival;
	public static final ConfigValue<Boolean> enableInCreative;
	public static final ConfigValue<Boolean> enableInSpectator;
	public static final ConfigValue<Boolean> enableInAdventure;
	public static final ConfigValue<Boolean> enhancedInfo;
	public static final ConfigValue<Boolean> enhancedStoreRender;
	public static final ConfigValue<Boolean> enableDepotRender;
	public static final ConfigValue<Boolean> enableOpenFilterScreen;
	public static final ConfigValue<Boolean> alwaysAllowRotating;
	static {
		BUILDER.push("ChainConveyor");
		alwaysAllowRiding = BUILDER.comment("Riding without wrench").define("alwaysAllowRiding", false);
		preventFalling = BUILDER.comment("Prevent falling off chain conveyors").define("preventFalling", false);
		separationDistance = BUILDER.comment("Minimum relative falling distance").define("separationDistance", 3);
		separationHeight = BUILDER.comment("Minimum relative falling height").define("separationHeight", -1);
		enhancedConnection = BUILDER.comment("Enhanced chain conveyor connection").define("enhancedConnection", true);
		BUILDER.pop();
		BUILDER.push("Flywheel");
		forcedBackend = BUILDER.comment("Force Allow flywheel backend").define("forcedBackend", false);
		BUILDER.pop();
		BUILDER.push("Goggles");
		enableInSurvival = BUILDER.comment("Enable goggles in Survival mode").define("enableInSurvival", true);
		enableInCreative = BUILDER.comment("Enable goggles in creative mode").define("enableInCreative", true);
		enableInSpectator = BUILDER.comment("Enable goggles in spectator mode").define("enableInSpectator", true);
		enableInAdventure = BUILDER.comment("Enable goggles in adventure mode").define("enableInAdventure", true);
		enhancedInfo = BUILDER.comment("Show more information in the goggles overlay").define("enhancedInfo", true);
		enhancedStoreRender = BUILDER.comment("Enhanced store rendering").define("enhancedStoreRender", true);
		enableDepotRender = BUILDER.comment("Render item stack tooltip on depot").define("enableDepotRender", true);
		enableOpenFilterScreen = BUILDER.comment("Open filter by hotkey").define("enableOpenFilterScreen", true);
		BUILDER.pop();
		BUILDER.push("Wrench");
		alwaysAllowRotating = BUILDER.comment("Always allow rotating block").define("alwaysAllowRotating", true);
		BUILDER.pop();
		CLIENT_SPEC = BUILDER.build();
	}
}
