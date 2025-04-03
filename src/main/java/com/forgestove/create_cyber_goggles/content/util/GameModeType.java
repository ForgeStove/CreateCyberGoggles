package com.forgestove.create_cyber_goggles.content.util;
import com.forgestove.create_cyber_goggles.Config;
import net.minecraft.world.level.GameType;

import java.util.EnumMap;

import static net.minecraft.world.level.GameType.*;
/**
 * 管理游戏模式与模组功能启用的配置映射关系。
 * <p>
 * 使用静态EnumMap将{@link GameType}与配置文件中的启用状态绑定，
 * 用于控制护目镜功能在不同游戏模式下的可用性：
 * <ul>
 *   <li>生存模式 - {@link Config#enableInSurvival}</li>
 *   <li>创造模式 - {@link Config#enableInCreative}</li>
 *   <li>旁观模式 - {@link Config#enableInSpectator}</li>
 *   <li>冒险模式 - {@link Config#enableInAdventure}</li>
 * </ul>
 * <p>
 * 配置映射在类加载时通过静态初始化块完成，后续可通过重新加载配置进行更新。
 */
public class GameModeType {
	public static final EnumMap<GameType, Boolean> MODE_CONFIG = new EnumMap<>(GameType.class);
	static {
		MODE_CONFIG.put(SURVIVAL, Config.enableInSurvival.get());
		MODE_CONFIG.put(CREATIVE, Config.enableInCreative.get());
		MODE_CONFIG.put(SPECTATOR, Config.enableInSpectator.get());
		MODE_CONFIG.put(ADVENTURE, Config.enableInAdventure.get());
	}
}
