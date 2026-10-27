package io.github.forgestove.create_cyber_goggles.compat.simulated;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.content.navigation_targets.*;
import dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability.ClientLodestonePositions;
import dev.simulated_team.simulated.index.SimDataComponents;
import io.github.forgestove.create_cyber_goggles.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.UUID;

import static io.github.forgestove.create_cyber_goggles.util.CCGUtil.mc;
public final class NavigationDistanceHelper {
	public static @Nullable Component line(ItemStack stack) {
		ClientLevel level = mc.level;
		LocalPlayer player = mc.player;
		if (level == null || player == null) return null;
		NavigationTarget target = NavigationTarget.ofStack(stack);
		if (target == null) return null;
		Resolved resolved = resolve(target, stack, level);
		if (resolved == null) return null;
		if (!resolved.sameDimension()) return CCGLang.translateDirect("tooltip.navigationDistance.otherDimension", ChatFormatting.GRAY);
		// 玩家在飞船（sublevel）内时，坐标须先投影到世界系
		Vector3d playerPos = Sable.HELPER.projectOutOfSubLevel(level, JOMLConversion.toJOML(player.position()));
		double distance = playerPos.distance(resolved.pos().x, resolved.pos().y, resolved.pos().z);
		return CCGLang.translateDirect(
			"tooltip.navigationDistance",
			ChatFormatting.GRAY,
			CCGLang.of().number(Mth.ceil(distance), ChatFormatting.GOLD).component()
		);
	}
	private static @Nullable Resolved resolve(NavigationTarget target, ItemStack stack, ClientLevel level) {
		if (target instanceof CompassNavigationTarget) return new Resolved(true, compassTarget(stack, level));
		if (target instanceof RecoveryCompassNavigationTarget) {
			GlobalPos death = deathLocation(stack);
			if (death == null) return null;
			return new Resolved(death.dimension().equals(level.dimension()), death.pos().getCenter());
		}
		return null;
	}
	private static Vec3 compassTarget(ItemStack stack, ClientLevel level) {
		UUID tracker = stack.get(SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER);
		if (tracker != null) {
			ClientLodestonePositions positions = ClientLodestonePositions.clientPositions.get(level);
			if (positions != null) {
				Vector3d pos = positions.CLIENT_LODESTONE_MAP.get(tracker);
				if (pos != null) return new Vec3(pos.x, pos.y, pos.z);
			}
		}
		return level.getSharedSpawnPos().getCenter();
	}
	/**
	 * 本地玩家取实时死亡点，其他玩家退回组件：{@code lastDeathLocation} 只在本人客户端同步。
	 */
	private static @Nullable GlobalPos deathLocation(ItemStack stack) {
		UUID placer = stack.get(SimDataComponents.COMPASS_PLACER_UUID);
		if (placer != null && mc.player != null && placer.equals(mc.player.getUUID())) return mc.player.getLastDeathLocation().orElse(null);
		return stack.get(SimDataComponents.LAST_PLAYER_DEATH_LOCATION);
	}
	private record Resolved(boolean sameDimension, Vec3 pos) {}
}
