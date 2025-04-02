package com.forgestove.create_cyber_goggles.content.util;
import com.forgestove.create_cyber_goggles.Config;
import net.minecraft.world.level.GameType;

import java.util.EnumMap;
public class GameModeType {
	public static final EnumMap<GameType, Boolean> MODE_CONFIG = new EnumMap<>(GameType.class);
	static {
		MODE_CONFIG.put(GameType.SURVIVAL, Config.enableInSurvival.get());
		MODE_CONFIG.put(GameType.CREATIVE, Config.enableInCreative.get());
		MODE_CONFIG.put(GameType.SPECTATOR, Config.enableInSpectator.get());
		MODE_CONFIG.put(GameType.ADVENTURE, Config.enableInAdventure.get());
	}
}
