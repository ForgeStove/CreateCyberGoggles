package com.ForgeStove.create_cyber_goggles;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;
public class Config {
	public static final Builder BUILDER = new Builder();
	public static final ModConfigSpec CONFIG_SPEC;
	public static ConfigValue<Boolean> AlwaysEnableGoggles;
	public static ConfigValue<Boolean> EnhancedGogglesInfo;
	public static ConfigValue<Boolean> AlwaysValidCogwheelPosition;
	public static ConfigValue<Boolean> AllowEmptyHandToRotate;
	public static ConfigValue<Boolean> AllowEmptyHandToRideChainConveyor;
	public static ConfigValue<Boolean> AllowFlywheelBackend;
	static {
		BUILDER.push("Client");
		AlwaysEnableGoggles = BUILDER.define("AlwaysEnableGoggles", true);
		EnhancedGogglesInfo = BUILDER.define("EnhancedGogglesInfo", true);
		AlwaysValidCogwheelPosition = BUILDER.define("AlwaysValidCogwheelPosition", true);
		AllowEmptyHandToRotate = BUILDER.define("AllowEmptyHandToRotation", true);
		AllowEmptyHandToRideChainConveyor = BUILDER.define("AllowEmptyHandToRideChainConveyor", true);
		AllowFlywheelBackend = BUILDER.define("AllowFlywheelBackend", false);
		CONFIG_SPEC = BUILDER.build();
		BUILDER.pop();
	}
}
