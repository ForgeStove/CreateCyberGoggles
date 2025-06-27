package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.content.kinetics.base.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.util.Common;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.*;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.jetbrains.annotations.*;

import java.awt.Color;
import java.util.*;
public class KineticDebugger {
	public static BlockPos lastSource;
	public static List<KineticBlockEntity> cachedKBEPath;
	public static void tick(RenderLevelStageEvent event) {
		if (!CCG.CONFIG.other.rainbowDebug) return;
		if (event.getStage() != Stage.AFTER_BLOCK_ENTITIES) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused() || mc.screen != null) return;
		var level = mc.level;
		if (level == null) return;
		var kbe = Common.getSelectedKBE();
		if (kbe == null) return;
		renderAxisLine(kbe);
		updateKBEPath(level, kbe);
		renderKineticPath(level, cachedKBEPath, System.currentTimeMillis(), event.getFrustum());
	}
	/**
	 * 更新并缓存当前选中动力方块实体的动力来源链路。
	 * <p>
	 * 如果选中的实体发生变化或缓存无效，则会重新构建链表，链表中的每个节点代表一个动力方块实体，
	 * 顺序为从动力源到当前选中实体。用于后续渲染动力链路。
	 *
	 * @param level 当前客户端世界
	 * @param kbe   当前选中的动力方块实体
	 */
	public static void updateKBEPath(ClientLevel level, KineticBlockEntity kbe) {
		if (kbe.source == lastSource && cachedKBEPath != null) return;
		// 构建源KBE链表
		var kbePath = new ArrayDeque<KineticBlockEntity>();
		var currentBE = kbe;
		while (currentBE != null) {
			kbePath.addFirst(currentBE); // 逆序插入，真源在前
			if (currentBE.source == null) break;
			currentBE = level.getBlockEntity(currentBE.source) instanceof KineticBlockEntity kbeSource ? kbeSource : null;
		}
		cachedKBEPath = new ArrayList<>(kbePath);
		lastSource = kbe.source;
	}
	/**
	 * 渲染整个动力链路，包括每个节点的包围盒轮廓和节点间的连线。
	 * <p>
	 * 仅在节点或连线在视锥体内时才进行渲染，以提升性能。
	 *
	 * @param level   当前客户端世界
	 * @param kbePath 动力链路节点列表（从源到目标）
	 * @param time    当前时间戳
	 * @param frustum 当前渲染视锥体
	 */
	public static void renderKineticPath(ClientLevel level, @NotNull List<KineticBlockEntity> kbePath, long time, Frustum frustum) {
		for (var depth = 0; depth < kbePath.size(); depth++) {
			var nodeBE = kbePath.get(depth);
			// 渲染前判断包围盒是否在视锥体内
			if (isAABBInFrustum(nodeBE, level, frustum)) renderOutline(nodeBE, level, depth, time);
			// 连线渲染时也判断两端是否有一端在视锥体内，否则跳过
			if (nodeBE.source == null) continue;
			if (isLineInFrustum(nodeBE.getBlockPos(), nodeBE.source, frustum)) renderKineticLine(nodeBE, getColor(depth, time));
		}
	}
	/**
	 * 判断包围盒是否在视锥体内。
	 *
	 * @param kbe     动力方块实体
	 * @param level   当前客户端世界
	 * @param frustum 视锥体
	 * @return 包围盒是否可见
	 */
	public static boolean isAABBInFrustum(@NotNull KineticBlockEntity kbe, @NotNull ClientLevel level, Frustum frustum) {
		var pos = kbe.getBlockPos();
		var shape = level.getBlockState(pos).getBlockSupportShape(level, pos);
		if (shape.isEmpty()) return false;
		var aabb = shape.bounds().move(pos);
		return frustum.isVisible(aabb);
	}
	/**
	 * 判断线段是否在视锥体内。
	 *
	 * @param from    起点
	 * @param to      终点
	 * @param frustum 视锥体
	 * @return 线段是否可见
	 */
	public static boolean isLineInFrustum(Vec3i from, Vec3i to, @NotNull Frustum frustum) {
		return frustum.isVisible(new AABB(VecHelper.getCenterOf(from), VecHelper.getCenterOf(to)));
	}
	/**
	 * 渲染指定 KineticBlockEntity 的包围盒轮廓。
	 *
	 * @param kbe   目标动力方块实体
	 * @param level 当前客户端世界
	 * @param depth 链路深度（用于着色）
	 * @param time  当前时间戳
	 */
	public static void renderOutline(@NotNull KineticBlockEntity kbe, @NotNull ClientLevel level, int depth, long time) {
		var toOutline = kbe.getBlockPos();
		var shape = level.getBlockState(toOutline).getBlockSupportShape(level, toOutline);
		if (kbe.getTheoreticalSpeed() == 0 || shape.isEmpty()) return;
		var rgb = getColor(depth, time);
		Outliner.getInstance().chaseAABB(toOutline.asLong(), shape.bounds().move(toOutline)).lineWidth(1 / 16f).colored(rgb);
	}
	/**
	 * 根据链路深度和时间生成彩虹色。
	 *
	 * @param depth 链路深度
	 * @param time  当前时间戳
	 * @return RGB 颜色值
	 */
	@Contract(pure = true)
	public static int getColor(int depth, long time) {
		var hue = 1.0f - (depth * 0.05f - (time % 6000L) / 3000f) % 1.0f;
		return Color.HSBtoRGB(hue, 0.8f, 1.0f);
	}
	/**
	 * 渲染动力链路的连线（非直接相邻）。
	 *
	 * @param kbe 当前动力方块实体
	 * @param rgb 线条颜色
	 */
	public static void renderKineticLine(@NotNull KineticBlockEntity kbe, int rgb) {
		if (kbe.source == null) return;
		var fromPos = kbe.getBlockPos();
		var toPos = kbe.source;
		if (fromPos.distManhattan(toPos) == 1) return;
		var from = VecHelper.getCenterOf(fromPos);
		var to = VecHelper.getCenterOf(toPos);
		Outliner.getInstance().showLine(fromPos.asLong() + toPos.asLong(), from, to).lineWidth(1 / 8f).colored(rgb);
	}
	/**
	 * 渲染动力方块的旋转轴线。
	 *
	 * @param kbe 目标动力方块实体
	 */
	public static void renderAxisLine(@NotNull KineticBlockEntity kbe) {
		var state = kbe.getBlockState();
		if (!(state.getBlock() instanceof IRotate iRotate)) return;
		var axis = iRotate.getRotationAxis(state);
		var vec = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, axis).getNormal());
		var center = VecHelper.getCenterOf(kbe.getBlockPos());
		Outliner.getInstance().showLine("axisLine", center.add(vec), center.subtract(vec)).lineWidth(1 / 8f);
	}
}
