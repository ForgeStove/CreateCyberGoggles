package com.forgestove.create_cyber_goggles;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;
public class Config {
	public static final Builder CLIENT = new Builder();
	public static final ModConfigSpec CLIENT_SPEC;
	// 交互相关配置
	public static final ConfigValue<Boolean> alwaysAllowRiding;
	public static final ConfigValue<Boolean> preventFalling;
	public static final ConfigValue<Integer> separationDistance;
	public static final ConfigValue<Integer> separationHeight;
	public static final ConfigValue<Boolean> enhancedConnection;
	public static final ConfigValue<Boolean> alwaysAllowRotating;
	// 渲染相关配置
	public static final ConfigValue<Boolean> removeBoxOverlay;
	public static final ConfigValue<Boolean> removeNetheriteFirstPerson;
	public static final ConfigValue<Boolean> removeDivingBootsAffect;
	public static final ConfigValue<Boolean> forcedBackend;
	public static final ConfigValue<Boolean> enableInSurvival;
	public static final ConfigValue<Boolean> enableInCreative;
	public static final ConfigValue<Boolean> enableInSpectator;
	public static final ConfigValue<Boolean> enableInAdventure;
	public static final ConfigValue<Boolean> enhancedInfo;
	public static final ConfigValue<Boolean> enhancedStoreRender;
	public static final ConfigValue<Boolean> renderExtraItems;
	public static final ConfigValue<Boolean> enableKineticEffect;
	public static final ConfigValue<Boolean> preciseNumbers;
	static {
		CLIENT.push("Interact");
		alwaysAllowRiding = CLIENT.define("alwaysAllowRiding", false);
		preventFalling = CLIENT.define("preventFalling", false);
		separationDistance = CLIENT.define("separationDistance", 3);
		separationHeight = CLIENT.define("separationHeight", -1);
		enhancedConnection = CLIENT.define("enhancedConnection", true);
		alwaysAllowRotating = CLIENT.define("alwaysAllowRotating", true);
		CLIENT.pop();
		CLIENT.push("Renderer");
		removeBoxOverlay = CLIENT.define("removeBoxOverlay", false);
		removeNetheriteFirstPerson = CLIENT.define("removeNetheriteFirstPerson", false);
		removeDivingBootsAffect = CLIENT.define("removeDivingBootsAffect", false);
		forcedBackend = CLIENT.define("forcedBackend", false);
		enableInSurvival = CLIENT.define("enableInSurvival", true);
		enableInCreative = CLIENT.define("enableInCreative", true);
		enableInSpectator = CLIENT.define("enableInSpectator", true);
		enableInAdventure = CLIENT.define("enableInAdventure", true);
		enhancedInfo = CLIENT.define("enhancedInfo", true);
		enhancedStoreRender = CLIENT.define("enhancedStoreRender", true);
		renderExtraItems = CLIENT.define("renderExtraItems", true);
		enableKineticEffect = CLIENT.define("enableKineticEffect", true);
		preciseNumbers = CLIENT.define("preciseNumbers", true);
		CLIENT.pop();
		CLIENT_SPEC = CLIENT.build();
	}
}
