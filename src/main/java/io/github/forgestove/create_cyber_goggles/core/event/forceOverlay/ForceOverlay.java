package io.github.forgestove.create_cyber_goggles.core.event.forceOverlay;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.force.*;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup.PointForce;
import dev.simulated_team.simulated.network.packets.contraption_diagram.RequestDiagramDataPacket;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.forceOverlay.ForceClusterer.Cluster;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
/**
 * 力覆盖层的客户端控制器。
 * <p>
 * 每个游戏刻执行射线投射以找到目标子层级，定期通过 {@link RequestDiagramDataPacket}
 * 从服务器请求力数据，并缓存接收到的聚类供渲染使用。
 */
public final class ForceOverlay {
	private static @Nullable UUID targetSubLevelId;
	private static @Nullable Map<ResourceLocation, List<Cluster>> smoothedClusters;
	private static double lastMass;
	private static long lastHeartbeatTick = Long.MIN_VALUE;
	private static long localTick;
	private static boolean hadData;
	public static void tick(Post ignoredEvent) {
		localTick++;
		var player = mc.player;
		var level = mc.level;
		var forceOverlay = CCG.config.aeronautics.forceOverlay;
		if (player == null || level == null || !forceOverlay.forceOverlayEnabled) {
			clear();
			return;
		}
		var newTarget = raycastTargetSubLevel(player);
		if (newTarget == null) {
			clear();
			return;
		}
		// 目标改变 → 重置状态
		if (!newTarget.equals(targetSubLevelId)) {
			targetSubLevelId = newTarget;
			smoothedClusters = null;
			lastMass = 0;
			hadData = false;
			lastHeartbeatTick = localTick - forceOverlay.heartbeatIntervalTicks;
		}
		// 每 N 个游戏刻请求一次数据
		if (localTick - lastHeartbeatTick >= forceOverlay.heartbeatIntervalTicks) {
			sendToServer(new RequestDiagramDataPacket(newTarget));
			lastHeartbeatTick = localTick;
		}
		// 如果自上一刻以来新数据到达，则重新聚类
		if (ForceDataCache.consumeDirty()) {
			var rawForces = ForceDataCache.getLatestForces();
			if (rawForces != null) {
				lastMass = ForceDataCache.getLatestMass();
				recomputeSmoothedClusters(rawForces);
				hadData = true;
			}
		}
		// 如果未及时收到心跳响应，则使旧数据过期
		if (hadData && localTick - lastHeartbeatTick > 30) {
			smoothedClusters = null;
			hadData = false;
		}
	}
	private static void clear() {
		targetSubLevelId = null;
		smoothedClusters = null;
		lastMass = 0;
		hadData = false;
		lastHeartbeatTick = -10;
		ForceDataCache.clear();
	}
	@Nullable
	private static UUID raycastTargetSubLevel(LocalPlayer player) {
		var chunks = CCG.config.aeronautics.forceOverlay.targetingChunks;
		var maxDist = chunks * 16.0;
		var hit = player.pick(maxDist, 1.0f, false);
		if (hit.getType() != Type.BLOCK) return null;
		var blockHit = (BlockHitResult) hit;
		var containing = Sable.HELPER.getContainingClient(blockHit.getLocation());
		return containing != null ? containing.getUniqueId() : null;
	}
	private static void recomputeSmoothedClusters(
		Map<ForceGroup, List<PointForce>> rawForces
	) {
		var angleThreshold = CCG.config.aeronautics.forceOverlay.clusterAngleRadians;
		var alpha = CCG.config.aeronautics.forceOverlay.smoothingFactor;
		Map<ResourceLocation, List<Cluster>> next = new Object2ObjectOpenHashMap<>();
		for (var e : rawForces.entrySet()) {
			var key = ForceGroups.REGISTRY.getKey(e.getKey());
			if (key == null) continue;
			var forces = e.getValue();
			if (forces.isEmpty()) continue;
			// 始终进行聚类——即使是单个力也能从位置混合中受益
			var rawClusters = ForceClusterer.cluster(forces, angleThreshold);
			if (rawClusters.isEmpty()) continue;
			// 与之前的聚类进行混合（指数移动平均）
			List<Cluster> prev = smoothedClusters != null ? smoothedClusters.getOrDefault(key, List.of()) : List.of();
			if (prev.isEmpty()) next.put(key, rawClusters);
			else next.put(key, blendClusters(rawClusters, prev, alpha));
		}
		smoothedClusters = next;
	}
	private static List<Cluster> blendClusters(List<Cluster> raw, List<Cluster> prev, double alpha) {
		var used = new boolean[prev.size()];
		List<Cluster> out = new ArrayList<>(raw.size());
		for (var n : raw) {
			var bestIdx = -1;
			var bestScore = 0.5; // 匹配的最小余弦相似度
			for (var i = 0; i < prev.size(); i++) {
				if (used[i]) continue;
				var p = prev.get(i);
				var na2 = n.force().lengthSquared();
				var pa2 = p.force().lengthSquared();
				if (na2 <= 0 || pa2 <= 0) continue;
				var cos = n.force().dot(p.force()) / Math.sqrt(na2 * pa2);
				if (cos > bestScore) {
					bestScore = cos;
					bestIdx = i;
				}
			}
			if (bestIdx >= 0) {
				used[bestIdx] = true;
				var p = prev.get(bestIdx);
				var pos = new Vector3d(p.pos()).lerp(n.pos(), alpha);
				var force = new Vector3d(p.force()).lerp(n.force(), alpha);
				out.add(new Cluster(pos, force, n.groupSize()));
			} else out.add(new Cluster(new Vector3d(n.pos()), new Vector3d(n.force()), n.groupSize()));
		}
		return out;
	}
	// ---- 渲染器的公共访问器 ----
	@Nullable
	public static UUID currentTarget() {
		return targetSubLevelId;
	}
	public static double currentMass() {
		return lastMass;
	}
	public static boolean hasData() {
		return hadData && smoothedClusters != null;
	}
	@Nullable
	public static Map<ResourceLocation, List<Cluster>> smoothedClusters() {
		return smoothedClusters;
	}
}
