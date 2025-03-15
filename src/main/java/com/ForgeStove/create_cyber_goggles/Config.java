package com.ForgeStove.create_cyber_goggles;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;
public class Config {
	public static final Builder BUILDER = new Builder();
	public static final ModConfigSpec CONFIG_SPEC;
	public static ConfigValue<Boolean> EnableGogglesWhenSurvival;
	public static ConfigValue<Boolean> EnableGogglesWhenCreative;
	public static ConfigValue<Boolean> EnableGogglesWhenSpectator;
	public static ConfigValue<Boolean> EnableGogglesWhenAdventure;
	public static ConfigValue<Boolean> EnhancedGogglesInfo;
	public static ConfigValue<Boolean> AlwaysAllowRotateBlocks;
	public static ConfigValue<Boolean> AlwaysAllowRideChainConveyor;
	public static ConfigValue<Boolean> PreventFallingFromChainConveyor;
	public static ConfigValue<Integer> ChainConveyorSeparationDistance;
	public static ConfigValue<Integer> ChainConveyorSeparationHeight;
	public static ConfigValue<Boolean> EnhancedChainConnection;
	public static ConfigValue<Boolean> AllowForcedFlywheelBackend;
	static {
		BUILDER.push("Goggles");
		EnableGogglesWhenSurvival = BUILDER.comment("Enable Goggles when in survival mode")
				.define("EnableGogglesWhenSurvival", true);
		EnableGogglesWhenCreative = BUILDER.comment("Enable Goggles when in creative mode")
				.define("EnableGogglesWhenCreative", true);
		EnableGogglesWhenSpectator = BUILDER.comment("Enable Goggles when in spectator mode")
				.define("EnableGogglesWhenSpectator", true);
		EnableGogglesWhenAdventure = BUILDER.comment("Enable Goggles when in adventure mode")
				.define("EnableGogglesWhenAdventure", true);
		EnhancedGogglesInfo = BUILDER.comment("Show enhanced information in the Goggles overlay")
				.define("EnhancedGogglesInfo", true);
		BUILDER.pop();
		BUILDER.push("Wrench");
		AlwaysAllowRotateBlocks = BUILDER.comment("Always allow rotating blocks, even if you don't equip the wrench")
				.define("AlwaysAllowRotateBlocks", true);
		BUILDER.pop();
		BUILDER.push("ChainConveyor");
		AlwaysAllowRideChainConveyor = BUILDER.comment(
						"Always allow rideable chain conveyors, even if you don't equip the wrench")
				.define("AlwaysAllowRideChainConveyor", false);
		PreventFallingFromChainConveyor = BUILDER.comment(
						"Prevent falling from chain conveyors, may cause issues if you can't go through blocks")
				.define("PreventFallingFromChainConveyor", false);
		ChainConveyorSeparationDistance = BUILDER.comment("The separation distance between you and the chain conveyor")
				.define("ChainConveyorSeparationDistance", 3);
		ChainConveyorSeparationHeight = BUILDER.comment("The separation height between you and the chain conveyor")
				.define("ChainConveyorSeparationHeight", -1);
		EnhancedChainConnection = BUILDER.comment("Enhance connecting between chain conveyors")
				.define("EnhancedChainConnection", false);
		BUILDER.pop();
		BUILDER.push("Flywheel");
		AllowForcedFlywheelBackend = BUILDER.comment("Allow flywheel backend, even if not compatible with shaders")
				.define("AllowForcedFlywheelBackend", false);
		BUILDER.pop();
		CONFIG_SPEC = BUILDER.build();
	}
}
