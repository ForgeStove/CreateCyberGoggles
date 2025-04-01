package com.forgestove.create_cyber_goggles.content.util;
import com.forgestove.create_cyber_goggles.Config;
import net.minecraft.world.level.GameType;

import java.util.EnumMap;

import static net.minecraft.world.level.GameType.*;
public record GameModeType() {
	public static final EnumMap<GameType, Boolean> MODE_CONFIG = new EnumMap<>(GameType.class);
	static {
		MODE_CONFIG.put(SURVIVAL, Config.enableInSurvival.get());
		MODE_CONFIG.put(CREATIVE, Config.enableInCreative.get());
		MODE_CONFIG.put(SPECTATOR, Config.enableInSpectator.get());
		MODE_CONFIG.put(ADVENTURE, Config.enableInAdventure.get());
	}
}
