package com.forgestove.create_cyber_goggles.content.util;
import com.forgestove.create_cyber_goggles.Config;
import com.simibubi.create.AllItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.GameType;

import java.util.EnumMap;

import static net.minecraft.world.level.GameType.*;
public class BoolValue {
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
	public static final EnumMap<GameType, Boolean> GAME_TYPE_MAP = new EnumMap<>(GameType.class);
	static {
		GAME_TYPE_MAP.put(SURVIVAL, Config.enableInSurvival.get());
		GAME_TYPE_MAP.put(CREATIVE, Config.enableInCreative.get());
		GAME_TYPE_MAP.put(SPECTATOR, Config.enableInSpectator.get());
		GAME_TYPE_MAP.put(ADVENTURE, Config.enableInAdventure.get());
	}
	public static boolean testForStealth(LocalPlayer player) {
		return Config.cardBoardedYourself.get()
				&& !player.getAbilities().flying
				&& AllItems.CARDBOARD_HELMET.isIn(player.getItemBySlot(EquipmentSlot.HEAD))
				&& AllItems.CARDBOARD_CHESTPLATE.isIn(player.getItemBySlot(EquipmentSlot.CHEST))
				&& AllItems.CARDBOARD_LEGGINGS.isIn(player.getItemBySlot(EquipmentSlot.LEGS))
				&& AllItems.CARDBOARD_BOOTS.isIn(player.getItemBySlot(EquipmentSlot.FEET));
	}
}
