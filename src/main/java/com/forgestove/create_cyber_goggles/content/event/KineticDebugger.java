package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.content.config.*;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.content.kinetics.base.*;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.*;

import java.awt.Color;
import java.util.*;
public class KineticDebugger {
	public static void tick() {
		if (!CCGConfig.config.other.rainbowDebug) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused()) return;
		var kbe = Common.getSelectedKBE();
		if (kbe == null) return;
		// 递归从真源到当前方块，depth=0为真源
		var level = mc.level;
		if (level == null) return;
		var source = findTrueSource(kbe, level, new HashSet<>());
		renderOutlineFromSource(source, kbe, 0);
		renderAxisLine(kbe);
	}
	// 递归查找真源
	@Contract("null, _, _ -> null")
	public static KineticBlockEntity findTrueSource(KineticBlockEntity kbe, ClientLevel level, Set<KineticBlockEntity> visited) {
		if (kbe == null || visited.contains(kbe) || kbe.source == null) return kbe;
		visited.add(kbe);
		var be = level.getBlockEntity(kbe.source);
		if (be instanceof KineticBlockEntity kbeSource) return findTrueSource(kbeSource, level, visited);
		return kbe;
	}
	// 从真源递归到目标kbe，depth=0为真源，递归到目标时停止
	@Contract("null, _, _ -> false")
	public static boolean renderOutlineFromSource(KineticBlockEntity current, KineticBlockEntity target, int depth) {
		if (current == null) return false;
		var toOutline = current.getBlockPos();
		var level = Minecraft.getInstance().level;
		if (level == null) return false;
		// 渲染方块轮廓
		var shape = level.getBlockState(toOutline).getBlockSupportShape(level, toOutline);
		if (current.getTheoreticalSpeed() == 0 || shape.isEmpty()) return false;
		var time = System.currentTimeMillis();
		var hue = 1.0f - (depth * 0.05f - (time % 6000L) / 3000f) % 1.0f;
		var rgb = Color.HSBtoRGB(hue, 0.8f, 1.0f);
		Outliner.getInstance().chaseAABB("kineticSource" + toOutline, shape.bounds().move(toOutline)).lineWidth(1 / 16f).colored(rgb);
		// 渲染连接线
		if (current.source != null) renderKineticLine(current, rgb);
		// 如果当前就是目标方块，递归结束
		if (current == target) return true;
		// 递归到所有下游（即所有以current为source的KBE），但这里只递归目标路径
		var next = findNextKineticBlockEntity(current, target);
		if (next != null) return renderOutlineFromSource(next, target, depth + 1);
		return false;
	}
	// 查找下一个在source链上的KBE
	public static @Nullable KineticBlockEntity findNextKineticBlockEntity(KineticBlockEntity current, KineticBlockEntity target) {
		// 沿着target的source链向上查找，直到current
		var level = Minecraft.getInstance().level;
		if (level == null) return null;
		var kbe = target;
		while (kbe != null && kbe != current) {
			if (kbe.source == null) break;
			var be = level.getBlockEntity(kbe.source);
			if (be instanceof KineticBlockEntity kbeSource) {
				if (kbeSource == current) return kbe;
				kbe = kbeSource;
			} else break;
		}
		return null;
	}
	// 渲染连接线
	public static void renderKineticLine(@NotNull KineticBlockEntity kbe, int rgb) {
		if (kbe.source == null) return;
		var fromPos = kbe.getBlockPos();
		var toPos = kbe.source;
		if (fromPos.distManhattan(toPos) == 1) return;
		var from = VecHelper.getCenterOf(fromPos);
		var to = VecHelper.getCenterOf(toPos);
		Outliner.getInstance().showLine("kineticLine" + fromPos + "->" + toPos, from, to).lineWidth(1 / 8f).colored(rgb);
	}
	// 渲染旋转轴线
	public static void renderAxisLine(@NotNull KineticBlockEntity kbe) {
		var state = kbe.getBlockState();
		if (!(state.getBlock() instanceof IRotate iRotate)) return;
		var axis = iRotate.getRotationAxis(state);
		var vec = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, axis).getNormal());
		var center = VecHelper.getCenterOf(kbe.getBlockPos());
		Outliner.getInstance().showLine("rotationAxis", center.add(vec), center.subtract(vec)).lineWidth(1 / 16f);
	}
}
