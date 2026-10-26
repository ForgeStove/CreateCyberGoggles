package io.github.forgestove.create_cyber_goggles.event.forceOverlay;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup.PointForce;
import org.joml.*;

import java.lang.Math;
import java.util.*;
/**
 * 根据力向量的角度相似性对 {@link PointForce} 进行聚类。
 * <p>
 * 指向相似方向的力被归为一组，并计算它们的加权平均位置用于箭头渲染。
 * <p>
 * 采用有界的固定 K 均值：最远点增量初始化 + 限量 Lloyd 迭代。每个力组的代价为
 * O(F·K·迭代数)，与力数量线性相关，避免大量力（例如大量磁力）时主线程卡顿。
 */
public class ForceClusterer {
	/** Lloyd 迭代上限。 */
	private static final int MAX_ITERATIONS = 8;
	/**
	 * @param forces                需要聚类的单个点力
	 * @param angleThresholdRadians 聚类内最大角度方差（弧度）
	 * @param maxClusters           每个力组的簇数上限（固定 K）
	 * @return 聚类列表，每个聚类包含合并后的位置和力向量
	 */
	public static List<Cluster> cluster(List<PointForce> forces, double angleThresholdRadians, int maxClusters) {
		int fSize = forces.size();
		if (fSize == 0) return List.of();
		int cap = Math.clamp(maxClusters, 1, fSize);
		// 展平数据，避免为每个力分配包装对象
		var px = new double[fSize];
		var py = new double[fSize];
		var pz = new double[fSize];
		var fx = new double[fSize];
		var fy = new double[fSize];
		var fz = new double[fSize];
		for (var i = 0; i < fSize; i++) {
			PointForce f = forces.get(i);
			Vector3dc p = f.point();
			Vector3dc v = f.force();
			px[i] = p.x();
			py[i] = p.y();
			pz[i] = p.z();
			fx[i] = v.x();
			fy[i] = v.y();
			fz[i] = v.z();
		}
		var cx = new double[cap];
		var cy = new double[cap];
		var cz = new double[cap];
		var assign = new int[fSize];
		var bestVar = new double[fSize];
		double thresholdSq = angleThresholdRadians * angleThresholdRadians;
		var n = 0;
		// 初始化首簇：绝对分量之和；退化（完全抵消）时取首个非零力方向
		var ax = 0.0;
		var ay = 0.0;
		var az = 0.0;
		for (var i = 0; i < fSize; i++) {
			ax += Math.abs(fx[i]);
			ay += Math.abs(fy[i]);
			az += Math.abs(fz[i]);
		}
		double len = Math.sqrt(ax * ax + ay * ay + az * az);
		if (len > 0) {
			cx[0] = ax / len;
			cy[0] = ay / len;
			cz[0] = az / len;
			n = 1;
		} else {
			for (var i = 0; i < fSize; i++) {
				double l = Math.sqrt(fx[i] * fx[i] + fy[i] * fy[i] + fz[i] * fz[i]);
				if (l <= 0) continue;
				cx[0] = fx[i] / l;
				cy[0] = fy[i] / l;
				cz[0] = fz[i] / l;
				n = 1;
				break;
			}
			if (n == 0) return List.of();
		}
		for (var i = 0; i < fSize; i++) bestVar[i] = angularVariance(fx[i], fy[i], fz[i], cx[0], cy[0], cz[0]);
		// 最远点增量初始化：每次新增一个簇，只做一遍 O(F) 增量更新
		while (n < cap) {
			int worst = -1;
			double worstVar = thresholdSq;
			for (var i = 0; i < fSize; i++)
				if (bestVar[i] > worstVar) {
					worstVar = bestVar[i];
					worst = i;
				}
			if (worst < 0) break;
			cx[n] = fx[worst];
			cy[n] = fy[worst];
			cz[n] = fz[worst];
			for (var i = 0; i < fSize; i++) {
				double v = angularVariance(fx[i], fy[i], fz[i], cx[n], cy[n], cz[n]);
				if (v < bestVar[i]) {
					bestVar[i] = v;
					assign[i] = n;
				}
			}
			n++;
		}
		// Lloyd 迭代
		for (var iter = 0; iter < MAX_ITERATIONS; iter++) {
			recomputeCentroids(assign, fx, fy, fz, cx, cy, cz, n);
			if (!assignAll(assign, fx, fy, fz, cx, cy, cz, n)) break;
		}
		recomputeCentroids(assign, fx, fy, fz, cx, cy, cz, n);
		// 输出：丢弃空簇与相互抵消的簇，位置按力在簇方向上的投影加权
		var posX = new double[n];
		var posY = new double[n];
		var posZ = new double[n];
		var cnt = new int[n];
		for (var i = 0; i < fSize; i++) {
			int c = assign[i];
			cnt[c]++;
			double lenSq = cx[c] * cx[c] + cy[c] * cy[c] + cz[c] * cz[c];
			if (lenSq <= 0) continue;
			double w = (cx[c] * fx[i] + cy[c] * fy[i] + cz[c] * fz[i]) / lenSq;
			posX[c] += w * px[i];
			posY[c] += w * py[i];
			posZ[c] += w * pz[i];
		}
		List<Cluster> out = new ArrayList<>(n);
		for (var c = 0; c < n; c++) {
			if (cnt[c] == 0) continue;
			if (cx[c] * cx[c] + cy[c] * cy[c] + cz[c] * cz[c] <= 0) continue;
			out.add(new Cluster(new Vector3d(posX[c], posY[c], posZ[c]), new Vector3d(cx[c], cy[c], cz[c]), cnt[c]));
		}
		return out;
	}
	private static double angularVariance(double ax, double ay, double az, double bx, double by, double bz) {
		double a2 = ax * ax + ay * ay + az * az;
		double b2 = bx * bx + by * by + bz * bz;
		if (a2 <= 0 || b2 <= 0) return 0;
		double cos = (ax * bx + ay * by + az * bz) / Math.sqrt(a2 * b2);
		if (cos > 1.0) cos = 1.0;
		else if (cos < -1.0) cos = -1.0;
		return 2.0 * (1.0 - cos);
	}
	/** 重新累计每个簇的合力（质心方向）。 */
	private static void recomputeCentroids(
		int[] assign,
		double[] fx,
		double[] fy,
		double[] fz,
		double[] cx,
		double[] cy,
		double[] cz,
		int n
	) {
		Arrays.fill(cx, 0, n, 0.0);
		Arrays.fill(cy, 0, n, 0.0);
		Arrays.fill(cz, 0, n, 0.0);
		for (var i = 0; i < fx.length; i++) {
			int c = assign[i];
			cx[c] += fx[i];
			cy[c] += fy[i];
			cz[c] += fz[i];
		}
	}
	/** 单遍重分配；返回是否已稳定（无变化）。空簇（零方向）会被跳过。 */
	private static boolean assignAll(int[] assign, double[] fx, double[] fy, double[] fz, double[] cx, double[] cy, double[] cz, int n) {
		var stable = true;
		for (var i = 0; i < fx.length; i++) {
			int prev = assign[i];
			int best = prev;
			double bestDist = Double.POSITIVE_INFINITY;
			for (var c = 0; c < n; c++) {
				if (cx[c] * cx[c] + cy[c] * cy[c] + cz[c] * cz[c] <= 0) continue;
				double v = angularVariance(fx[i], fy[i], fz[i], cx[c], cy[c], cz[c]);
				if (v < bestDist) {
					bestDist = v;
					best = c;
				}
			}
			if (best != prev) {
				assign[i] = best;
				stable = false;
			}
		}
		return stable;
	}
	/** 合并在一起的力聚类。 */
	public record Cluster(Vector3d pos, Vector3d force, int groupSize) {}
}
