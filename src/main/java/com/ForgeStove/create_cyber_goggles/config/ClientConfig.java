package com.ForgeStove.create_cyber_goggles.config;
import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

import static com.ForgeStove.create_cyber_goggles.config.ClientConfig.Comments.*;
@SuppressWarnings("unused") public class ClientConfig extends ConfigBase {
	// ChainConveyor Group
	public final ConfigGroup chainConveyor = group(1, "ChainConveyor", CHAIN_CONVEYOR);
	public final ConfigBool alwaysAllowRide = b(false, "alwaysAllowRide", ALWAYS_ALLOW_RIDE);
	public final ConfigBool preventFalling = b(false, "preventFalling", PREVENT_FALLING);
	public final ConfigInt separationDistance = i(3, "separationDistance", SEPARATION_DISTANCE);
	public final ConfigInt separationHeight = i(-1, "separationHeight", SEPARATION_HEIGHT);
	public final ConfigBool enhancedConnection = b(false, "enhancedConnection", ENHANCED_CONNECTION);
	// Flywheel Group
	public final ConfigGroup flywheel = group(1, "Flywheel", FLYWHEEL);
	public final ConfigBool forcedBackend = b(false, "forcedBackend", FORCED_BACKEND);
	// Goggles Group
	public final ConfigGroup goggles = group(1, "Goggles", GOGGLES);
	public final ConfigBool enableOnSurvival = b(true, "enableOnSurvival", ENABLE_ON_SURVIVAL);
	public final ConfigBool enableOnCreative = b(true, "enableOnCreative", ENABLE_ON_CREATIVE);
	public final ConfigBool enableOnSpectator = b(true, "enableOnSpectator", ENABLE_ON_SPECTATOR);
	public final ConfigBool enableOnAdventure = b(true, "enableOnAdventure", ENABLE_ON_ADVENTURE);
	public final ConfigBool enhancedInfo = b(true, "enhancedInfo", ENHANCED_INFO);
	// Wrench Group
	public final ConfigGroup wrench = group(1, "Wrench", WRENCH);
	public final ConfigBool alwaysAllowRotate = b(true, "alwaysAllowRotate", ALWAYS_ALLOW_ROTATE);
	@Override public @NotNull String getName() {
		return "client";
	}
	static class Comments {
		// Group comments
		static final String CHAIN_CONVEYOR = "Chain Conveyor mechanics configuration";
		static final String FLYWHEEL = "Flywheel rendering settings";
		static final String GOGGLES = "Goggles related settings";
		static final String WRENCH = "Wrench behavior settings";
		// Field comments
		static final String ALWAYS_ALLOW_RIDE = "Enable conveyor riding without wrench";
		static final String PREVENT_FALLING = "Prevent falling off conveyors\nMay cause collision issues with blocks";
		static final String SEPARATION_DISTANCE = "Minimum relative falling distance between you and chain conveyor";
		static final String SEPARATION_HEIGHT = "Minimum relative falling height between you and chain conveyor";
		static final String ENHANCED_CONNECTION = "Enable improved chain conveyor connection";
		static final String FORCED_BACKEND = "Force enable Flywheel rendering backend\nCause issues with shaders";
		static final String ENABLE_ON_SURVIVAL = "Enable Goggles in Survival mode";
		static final String ENABLE_ON_CREATIVE = "Enable Goggles in Creative mode";
		static final String ENABLE_ON_SPECTATOR = "Enable Goggles in Spectator mode";
		static final String ENABLE_ON_ADVENTURE = "Enable Goggles in Adventure mode";
		static final String ENHANCED_INFO = "Show enhanced information in Goggles overlay";
		static final String ALWAYS_ALLOW_ROTATE = "Allow to rotate block without equipping wrench";
	}
}
