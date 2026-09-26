package io.github.forgestove.create_cyber_goggles.compat.simulated;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.content.navigation_targets.*;
import dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability.ClientLodestonePositions;
import dev.simulated_team.simulated.index.SimDataComponents;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * simulated 导航物的「玩家 → 目标」距离 tooltip。
 * <p>
 * 只处理目标位置与导航台无关的两类：指南针（世界出生点 / 磁石追踪）与回响指南针（上次死亡点）。
 * 地图、磁石、Explorer's / Nature's Compass 的目标都以导航台自身为参照算出，物品上算不出来，故跳过。
 * <p>
 * 注意不能改调 {@code NavigationTarget#getTarget}：它要求传入导航台方块实体，且回响指南针那条会写物品组件
 */
public final class NavigationDistanceHelper {
	public static @Nullable Component line(ItemStack stack) {
		var level = mc.level;
		var player = mc.player;
		if (level == null || player == null) return null;
		var target = NavigationTarget.ofStack(stack);
		if (target == null) return null;
		var resolved = resolve(target, stack, level);
		if (resolved == null) return null;
		if (!resolved.sameDimension())
			return Component.translatable("create_cyber_goggles.tooltip.navigationDistance.otherDimension").withStyle(ChatFormatting.GRAY);
		// 玩家可能站在飞船（sublevel）里，坐标须先投影到世界系，否则算出来的是飞船局部坐标
		var playerPos = Sable.HELPER.projectOutOfSubLevel(level, JOMLConversion.toJOML(player.position()));
		var distance = playerPos.distance(resolved.pos().x, resolved.pos().y, resolved.pos().z);
		return Component.translatable(
				"create_cyber_goggles.tooltip.navigationDistance",
				CCGLang.number(Mth.ceil(distance), ChatFormatting.GOLD).component()
			)
			.withStyle(ChatFormatting.GRAY);
	}
	private static @Nullable Resolved resolve(NavigationTarget target, ItemStack stack, ClientLevel level) {
		if (target instanceof CompassNavigationTarget) return new Resolved(true, compassTarget(stack, level));
		if (target instanceof RecoveryCompassNavigationTarget) {
			var death = deathLocation(stack);
			if (death == null) return null;
			return new Resolved(death.dimension().equals(level.dimension()), death.pos().getCenter());
		}
		return null;
	}
	private static Vec3 compassTarget(ItemStack stack, ClientLevel level) {
		var tracker = stack.get(SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER);
		if (tracker != null) {
			var positions = ClientLodestonePositions.clientPositions.get(level);
			if (positions != null) {
				var pos = positions.CLIENT_LODESTONE_MAP.get(tracker);
				if (pos != null) return new Vec3(pos.x, pos.y, pos.z);
			}
		}
		return level.getSharedSpawnPos().getCenter();
	}
	/**
	 * 与导航台同规则：UUID 对应的玩家在线时以其实时死亡点为准（物品上存的可能已过期），否则退回存的那个。
	 * 差别是这里不回写组件 —— 导航台会把在线玩家的值同步进组件，而 tooltip 期间不能改物品。
	 * <p>
	 * 只能对本地玩家取实时值：{@code Player#lastDeathLocation} 是普通字段（非 SynchedEntityData），
	 * MC 只在登录/重生时给「本人客户端」写入（见 ClientPacketListener 的 setLastDeathLocation），
	 * 其他玩家在客户端恒为空 —— 那种情况必须退回组件，否则会把本来算得出来的距离吞掉
	 */
	private static @Nullable GlobalPos deathLocation(ItemStack stack) {
		var placer = stack.get(SimDataComponents.COMPASS_PLACER_UUID);
		if (placer != null && mc.player != null && placer.equals(mc.player.getUUID())) return mc.player.getLastDeathLocation().orElse(null);
		return stack.get(SimDataComponents.LAST_PLAYER_DEATH_LOCATION);
	}
	private record Resolved(boolean sameDimension, Vec3 pos) {}
}
