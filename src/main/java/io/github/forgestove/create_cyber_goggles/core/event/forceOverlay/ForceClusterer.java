package io.github.forgestove.create_cyber_goggles.core.event.forceOverlay;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup.PointForce;
import org.joml.*;

import java.lang.Math;
import java.util.*;
/**
 * 根据力向量的角度相似性对 {@link PointForce} 进行聚类。
 * <p>
 * 指向相似方向的力被归为一组，并计算它们的加权平均位置用于箭头渲染。
 */
public class ForceClusterer {
	/**
	 * @param forces                需要聚类的单个点力
	 * @param angleThresholdRadians 聚类内最大角度方差（弧度）
	 * @return 聚类列表，每个聚类包含合并后的位置和力向量
	 */
	public static List<Cluster> cluster(List<PointForce> forces, double angleThresholdRadians) {
		if (forces.isEmpty()) return List.of();
		List<Working> clusters = new ArrayList<>();
		List<Indexed> indexed = new ArrayList<>(forces.size());
		for (var f : forces) indexed.add(new Indexed(f.point(), f.force()));
		var thresholdSq = angleThresholdRadians * angleThresholdRadians;
		while (tryAddCluster(clusters, indexed, thresholdSq)) while (!groupArrows(clusters, indexed)) organizeClusters(clusters, indexed);
		organizeClusters(clusters, indexed);
		finalizePositions(clusters, indexed);
		List<Cluster> out = new ArrayList<>(clusters.size());
		for (var w : clusters) out.add(new Cluster(new Vector3d(w.pos), new Vector3d(w.force), w.groupSize));
		return out;
	}
	private static boolean tryAddCluster(List<Working> clusters, List<Indexed> forces, double thresholdSq) {
		if (!clusters.isEmpty()) {
			var maxVar = -1.0;
			Indexed outlier = null;
			for (var f : forces) {
				var v = angularVariance(clusters.get(f.clusterIndex).force, f.force);
				if (v > maxVar) {
					maxVar = v;
					outlier = f;
				}
			}
			if (outlier != null && maxVar > thresholdSq) {
				var c = new Working();
				c.force.set(outlier.force);
				clusters.add(c);
				return true;
			}
			return false;
		}
		// 第一个聚类：以绝对分量之和作为初始方向
		var c = new Working();
		for (var f : forces) c.force.add(Math.abs(f.force.x()), Math.abs(f.force.y()), Math.abs(f.force.z()));
		if (c.force.lengthSquared() > 0) c.force.normalize();
		clusters.add(c);
		return true;
	}
	private static boolean groupArrows(List<Working> clusters, List<Indexed> forces) {
		var stable = true;
		for (var f : forces) {
			var prev = f.clusterIndex;
			var minDist = Double.POSITIVE_INFINITY;
			var best = prev;
			for (var i = 0; i < clusters.size(); i++) {
				var d = angularVariance(f.force, clusters.get(i).force);
				if (d < minDist) {
					minDist = d;
					best = i;
				}
			}
			f.clusterIndex = best;
			if (prev != best) stable = false;
		}
		return stable;
	}
	private static void organizeClusters(List<Working> clusters, List<Indexed> forces) {
		for (var c : clusters) {
			c.force.zero();
			c.groupSize = 0;
		}
		for (var f : forces) {
			var c = clusters.get(f.clusterIndex);
			c.force.add(f.force);
			c.groupSize++;
		}
		for (var k = clusters.size() - 1; k >= 0; k--)
			if (clusters.get(k).groupSize == 0) {
				clusters.remove(k);
				for (var f : forces) if (f.clusterIndex > k) f.clusterIndex--;
			}
	}
	private static void finalizePositions(List<Working> clusters, List<Indexed> forces) {
		for (var f : forces) {
			var c = clusters.get(f.clusterIndex);
			var lenSq = c.force.lengthSquared();
			if (lenSq <= 0) continue;
			// 根据力在聚类方向上的投影加权位置贡献
			c.pos.fma(c.force.dot(f.force) / lenSq, f.pos);
		}
	}
	private static double angularVariance(Vector3dc a, Vector3dc b) {
		var a2 = a.dot(a);
		var b2 = b.dot(b);
		if (a2 <= 0 || b2 <= 0) return 0;
		var ab = a.dot(b);
		return 2.0 * (1.0 - ab / Math.sqrt(a2 * b2));
	}
	/** 合并在一起的力聚类。 */
	public record Cluster(Vector3d pos, Vector3d force, int groupSize) {}
	private static final class Working {
		final Vector3d pos = new Vector3d();
		final Vector3d force = new Vector3d();
		int groupSize;
	}
	private static final class Indexed {
		final Vector3dc pos;
		final Vector3dc force;
		int clusterIndex;
		Indexed(Vector3dc pos, Vector3dc force) {
			this.pos = pos;
			this.force = force;
		}
	}
}
