package io.github.forgestove.create_cyber_goggles.core.overlay;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup.PointForce;
import org.joml.*;

import java.lang.Math;
import java.util.*;
/**
 * Clusters {@link PointForce}s by angular similarity of their force vectors.
 * <p>
 * Forces pointing in similar directions are grouped together, and their weighted-average
 * position is computed for arrow rendering.
 */
public final class ForceClusterer {
	/**
	 * @param forces                individual point forces to cluster
	 * @param angleThresholdRadians maximum angular variance within a cluster
	 * @return list of clusters, each with a merged position and force vector
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
		// First cluster: sum of absolute components as initial direction
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
			// Weight position contribution by force projection onto cluster direction
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
	/** A cluster of forces merged together. */
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
