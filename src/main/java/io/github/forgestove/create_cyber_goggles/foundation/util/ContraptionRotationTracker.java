package io.github.forgestove.create_cyber_goggles.foundation.util;
import com.simibubi.create.content.contraptions.*;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity.ContraptionRotationState;
import com.simibubi.create.foundation.collision.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import org.joml.*;

import java.lang.Math;
import java.lang.ref.WeakReference;
public class ContraptionRotationTracker {
	private static final Quaternionf currentRotation = new Quaternionf();
	private static final Quaternionf lastInverse = new Quaternionf();
	private static final Quaternionf deltaRotation = new Quaternionf();
	private static AbstractContraptionEntity tracked;
	private static WeakReference<AbstractContraptionEntity> poseContraption = new WeakReference<>(null);
	private static Vec3 poseAnchor = Vec3.ZERO;
	private static ContraptionRotationState poseRotation;
	/**
	 * 记录玩家脚下结构当前 tick 的位姿。玩家位置由 Create 在 tick 末尾按这个位姿修正，
	 * 下一个 tick 内的支撑判定必须用这个位姿才能和玩家实际位置对齐。
	 */
	public static void refreshPose(Player player) {
		var contraption = find(player);
		if (contraption == null || !hasRotation(contraption)) {
			poseContraption = new WeakReference<>(null);
			poseRotation = null;
			return;
		}
		poseContraption = new WeakReference<>(contraption);
		poseAnchor = contraption.getAnchorVec();
		poseRotation = contraption.getRotationState();
	}
	/**
	 * 返回结构自上一 tick 以来的旋转增量，不在结构上时返回 null。
	 * 返回的是共享实例，下次调用会被覆盖。
	 */
	public static Quaternionf delta() {
		var contraption = poseContraption.get();
		if (contraption == null || poseRotation == null) {
			tracked = null;
			return null;
		}
		var x = contraption.applyRotation(new Vec3(1, 0, 0), 1);
		var y = contraption.applyRotation(new Vec3(0, 1, 0), 1);
		var z = contraption.applyRotation(new Vec3(0, 0, 1), 1);
		currentRotation.set(new Matrix3f(
			(float) x.x,
			(float) x.y,
			(float) x.z,
			(float) y.x,
			(float) y.y,
			(float) y.z,
			(float) z.x,
			(float) z.y,
			(float) z.z
		).getNormalizedRotation(new Quaternionf()));
		if (contraption != tracked) {
			tracked = contraption;
			lastInverse.set(currentRotation).conjugate();
			return null;
		}
		deltaRotation.set(currentRotation).mul(lastInverse);
		lastInverse.set(currentRotation).conjugate();
		return deltaRotation;
	}
	public static void reset() {
		tracked = null;
	}
	/** 当前是否站在结构上 */
	public static boolean isOnContraption() {
		return poseContraption.get() != null && poseRotation != null;
	}
	/** 结构绕世界 Y 轴的旋转角（弧度），用作边缘判定的局部坐标系 */
	public static float frameYaw() {
		var contraption = poseContraption.get();
		if (contraption == null) return 0;
		var xAxis = contraption.applyRotation(new Vec3(1, 0, 0), 1);
		return (float) Math.atan2(-xAxis.z, xAxis.x);
	}
	/** 结构偏航系中的局部偏移 (x, z) 处是否踩空：既无世界方块也无结构支撑 */
	public static boolean wouldSlideOff(Player player, Quaternionf frame, double x, double z, float distance) {
		var movement = frame.transform(new Vector3f((float) x, 0, (float) z));
		var bounds = player.getBoundingBox();
		var probe = new AABB(
			bounds.minX + movement.x,
			bounds.minY - distance - 1.0E-5,
			bounds.minZ + movement.z,
			bounds.maxX + movement.x,
			bounds.minY,
			bounds.maxZ + movement.z
		);
		return player.level().noCollision(player, probe) && !hasSupport(player, probe);
	}
	public static boolean hasSupport(Player player, AABB probe) {
		if (!player.isLocalPlayer()) return false;
		var cached = poseContraption.get();
		if (cached != null && poseRotation != null && cached.isAlive() && hasSupport(cached, probe, poseAnchor, poseRotation)) return true;
		for (WeakReference<AbstractContraptionEntity> ref : ContraptionHandler.loadedContraptions.get(player.level()).values()) {
			var entity = ref.get();
			if (entity == null || entity == cached || !entity.isAlive()) continue;
			var bounds = entity.getBoundingBox();
			if (!bounds.inflate(bounds.getSize()).intersects(probe)) continue;
			if (hasSupport(entity, probe, entity.getAnchorVec(), entity.getRotationState())) return true;
		}
		return false;
	}
	private static boolean hasSupport(AbstractContraptionEntity entity, AABB probe, Vec3 anchor, ContraptionRotationState rotation) {
		var contraption = entity.getContraption();
		if (contraption == null) return false;
		var colliders = contraption.getSimplifiedEntityColliders();
		if (colliders == null || colliders.size == 0) return false;
		var center = ContraptionCollider.worldToLocalPos(probe.getCenter(), anchor, rotation);
		var obb = new OrientedBB(center, new Vec3(probe.getXsize() / 2, probe.getYsize() / 2, probe.getZsize() / 2), rotation.asMatrix());
		var result = ContinuousOBBCollider.collideMany(colliders, new CollisionList(), obb, Vec3.ZERO, 0, true);
		return result.surfaceCollision || !result.collisionResponse.equals(Vec3.ZERO);
	}
	private static boolean hasRotation(AbstractContraptionEntity entity) {
		return !(entity instanceof ControlledContraptionEntity controlled) || controlled.getRotationAxis() != null;
	}
	private static AbstractContraptionEntity find(Player player) {
		for (WeakReference<AbstractContraptionEntity> ref : ContraptionHandler.loadedContraptions.get(player.level()).values()) {
			var contraption = ref.get();
			if (contraption == null || !contraption.isAlive()) continue;
			var contact = contraption.collidingEntities.get(player);
			if (contact != null && contact.intValue() == 0) return contraption;
		}
		return null;
	}
}
