package io.github.forgestove.create_cyber_goggles.core.overlay;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.force.*;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup.PointForce;
import dev.simulated_team.simulated.network.packets.contraption_diagram.RequestDiagramDataPacket;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.overlay.ForceClusterer.Cluster;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.*;
/**
 * Client-side controller for the force overlay.
 * <p>
 * On each tick it ray-casts to find the targeted sublevel, periodically
 * requests force data from the server via {@link RequestDiagramDataPacket},
 * and caches the received clusters for rendering.
 */
public final class ForceOverlayClient {
	private static final long HEARTBEAT_INTERVAL_TICKS = 10;
	private static final long SNAPSHOT_TTL_TICKS = 30;
	private static @Nullable UUID targetSubLevelId;
	private static @Nullable Map<ResourceLocation, List<Cluster>> smoothedClusters;
	private static double lastMass;
	private static long lastHeartbeatTick = -10;
	private static long localTick;
	private static boolean hadData;
	private ForceOverlayClient() {
	}
	/** Called every client tick via {@link Post}. */
	public static void tick(Post ignoredEvent) {
		localTick++;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		var level = mc.level;
		if (player == null || level == null || !CCG.config.forceOverlay.enabled) {
			clear();
			return;
		}
		var newTarget = raycastTargetSubLevel(player);
		if (newTarget == null) {
			clear();
			return;
		}
		// Target changed → reset state
		if (!newTarget.equals(targetSubLevelId)) {
			targetSubLevelId = newTarget;
			smoothedClusters = null;
			lastMass = 0;
			hadData = false;
			lastHeartbeatTick = localTick - HEARTBEAT_INTERVAL_TICKS; // force immediate request
		}
		// Heartbeat: request fresh data every 10 ticks
		if (localTick - lastHeartbeatTick >= HEARTBEAT_INTERVAL_TICKS) {
			PacketDistributor.sendToServer(new RequestDiagramDataPacket(newTarget));
			lastHeartbeatTick = localTick;
		}
		// If new data arrived since last tick, re-cluster
		if (ForceDataCache.consumeDirty()) {
			var rawForces = ForceDataCache.getLatestForces();
			if (rawForces != null) {
				lastMass = ForceDataCache.getLatestMass();
				recomputeSmoothedClusters(rawForces);
				hadData = true;
			}
		}
		// Expire old data if we haven't received a heartbeat response in time
		if (hadData && localTick - lastHeartbeatTick > SNAPSHOT_TTL_TICKS) {
			smoothedClusters = null;
			hadData = false;
		}
	}
	private static void recomputeSmoothedClusters(
		Map<ForceGroup, List<PointForce>> rawForces
	) {
		var angleThreshold = CCG.config.forceOverlay.clusterAngleRadians;
		var alpha = CCG.config.forceOverlay.smoothingFactor;
		Map<ResourceLocation, List<Cluster>> next = new Object2ObjectOpenHashMap<>();
		for (var e : rawForces.entrySet()) {
			var key = ForceGroups.REGISTRY.getKey(e.getKey());
			if (key == null) continue;
			var forces = e.getValue();
			if (forces.isEmpty()) continue;
			// Always cluster — even single forces benefit from blended positions
			var rawClusters = ForceClusterer.cluster(forces, angleThreshold);
			if (rawClusters.isEmpty()) continue;
			// Blend with previous clusters (EMA)
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
			var bestScore = 0.5; // minimum cosine similarity to match
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
	@Nullable
	private static UUID raycastTargetSubLevel(LocalPlayer player) {
		var chunks = CCG.config.forceOverlay.targetingChunks;
		var maxDist = chunks * 16.0;
		var hit = player.pick(maxDist, 1.0f, false);
		if (hit.getType() != Type.BLOCK) return null;
		var blockHit = (BlockHitResult) hit;
		var containing = Sable.HELPER.getContainingClient(blockHit.getLocation());
		return containing != null ? containing.getUniqueId() : null;
	}
	// ---- Public accessors for renderers ----
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
	private static void clear() {
		targetSubLevelId = null;
		smoothedClusters = null;
		lastMass = 0;
		hadData = false;
		lastHeartbeatTick = -10;
		ForceDataCache.clear();
	}
}
