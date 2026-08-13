package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.kinetics.base.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.*;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class KineticDebugger {
	public static BlockPos lastSource;
	public static List<BlockPos> cachedKBEPath;
	public static void tick(Post ignoredEvent) {
		if (!CCG.config.outliner.rainbowDebug) return;
		if (shouldSuppressInfo()) return;
		if (mc.isPaused() || isInGUI() || mc.level == null) return;
		var kbe = getBlockEntity(KineticBlockEntity.class);
		if (kbe == null) return;
		renderAxisLine(kbe);
		updateKBEPath(mc.level, kbe);
		renderKineticPath(cachedKBEPath, mc.level.getGameTime());
	}
	/**
	 * 渲染动力方块的旋转轴线。
	 *
	 * @param kbe 目标动力方块实体
	 */
	public static void renderAxisLine(@NotNull KineticBlockEntity kbe) {
		var state = kbe.getBlockState();
		if (!(state.getBlock() instanceof IRotate iRotate)) return;
		var vec = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, iRotate.getRotationAxis(state)).getNormal());
		var center = VecHelper.getCenterOf(kbe.getBlockPos());
		outliner.showLine("RotationAxis", center.add(vec), center.subtract(vec)).lineWidth(1 / 8f);
	}
	/**
	 * 更新并缓存当前选中动力方块实体的动力来源链路。
	 * <p>
	 * 如果选中的实体发生变化或缓存无效，则会重新构建链表，链表中的每个节点代表一个动力方块实体的坐标，
	 * 顺序为从动力源到当前选中实体。只存坐标避免静态引用持有方块实体（及整个世界）。
	 *
	 * @param level 当前客户端世界
	 * @param kbe   当前选中的动力方块实体
	 */
	public static void updateKBEPath(ClientLevel level, @NotNull KineticBlockEntity kbe) {
		if (kbe.source == lastSource && cachedKBEPath != null) return;
		var kbePath = new ArrayDeque<BlockPos>();
		var currentBE = kbe;
		while (currentBE != null) {
			kbePath.addFirst(currentBE.getBlockPos());
			if (currentBE.source == null) break;
			currentBE = level.getBlockEntity(currentBE.source) instanceof KineticBlockEntity kbeSource ? kbeSource : null;
		}
		cachedKBEPath = List.copyOf(kbePath);
		lastSource = kbe.source;
	}
	/**
	 * 渲染整个动力链路，包括每个节点的包围盒轮廓和节点间的连线。
	 * <p>
	 * 仅在节点或连线在视锥体内时才进行渲染，以提升性能。方块实体按坐标现取，已被破坏或区块卸载时跳过。
	 *
	 * @param kbePath 动力链路节点坐标列表（从源到目标）
	 * @param time    当前时间戳
	 */
	public static void renderKineticPath(@NotNull List<BlockPos> kbePath, long time) {
		var level = mc.level;
		if (level == null) return;
		var frustum = mc.levelRenderer.getFrustum();
		for (var depth = 0; depth < kbePath.size(); depth++) {
			var blockPos = kbePath.get(depth);
			if (!(level.getBlockEntity(blockPos) instanceof KineticBlockEntity nodeBE)) continue;
			// 渲染前判断包围盒是否在视锥体内
			var rgb = getRainbowColor(depth, time);
			if (isAABBInFrustum(nodeBE, frustum)) renderOutline(nodeBE, depth, rgb);
			// 连线渲染时也判断两端是否有一端在视锥体内，否则跳过
			if (nodeBE.source == null) continue;
			if (isLineInFrustum(blockPos, nodeBE.source, frustum)) renderKineticLine(nodeBE, depth, rgb);
		}
	}
	/**
	 * 根据链路深度和时间生成彩虹色。
	 *
	 * @param depth 链路深度
	 * @param time  当前时间戳
	 * @return RGB 颜色值
	 */
	public static int getRainbowColor(int depth, long time) {
		return Color.HSBtoRGB(1.0f - (depth * 0.05f - (time % 50L) / 50f) % 1.0f, 0.8f, 1.0f);
	}
	/**
	 * 判断包围盒是否在视锥体内。
	 *
	 * @param kbe     动力方块实体
	 * @param frustum 视锥体
	 * @return 包围盒是否可见
	 */
	public static boolean isAABBInFrustum(@NotNull KineticBlockEntity kbe, Frustum frustum) {
		if (mc.level == null) return false;
		var pos = kbe.getBlockPos();
		var shape = mc.level.getBlockState(pos).getBlockSupportShape(mc.level, pos);
		if (shape.isEmpty()) return false;
		return frustum.isVisible(shape.bounds().move(pos));
	}
	/**
	 * 渲染指定{@link KineticBlockEntity}的包围盒轮廓。
	 *
	 * @param kbe   目标动力方块实体
	 * @param depth 链路深度
	 * @param rgb   轮廓的RGB颜色值
	 */
	public static void renderOutline(@NotNull KineticBlockEntity kbe, int depth, int rgb) {
		if (kbe.getTheoreticalSpeed() == 0) return;
		var blockPos = kbe.getBlockPos();
		outliner.chaseAABB("KineticOutline" + depth, getBounds(blockPos)).lineWidth(1 / 16f).colored(rgb);
	}
	/**
	 * 判断线段是否在视锥体内。
	 *
	 * @param start   起点
	 * @param end     终点
	 * @param frustum 视锥体
	 * @return 线段是否可见
	 */
	public static boolean isLineInFrustum(Vec3i start, Vec3i end, @NotNull Frustum frustum) {
		return frustum.isVisible(new AABB(VecHelper.getCenterOf(start), VecHelper.getCenterOf(end)));
	}
	/**
	 * 渲染动力链路的连线（非直接相邻）。
	 *
	 * @param kbe   当前动力方块实体
	 * @param depth 链路深度
	 * @param rgb   线条颜色
	 */
	public static void renderKineticLine(@NotNull KineticBlockEntity kbe, int depth, int rgb) {
		if (kbe.source == null) return;
		var start = kbe.getBlockPos();
		var end = kbe.source;
		if (start.distManhattan(end) == 1) return;
		outliner.showLine("KineticLine" + depth, VecHelper.getCenterOf(start), VecHelper.getCenterOf(end)).lineWidth(1 / 8f).colored(rgb);
	}
}
