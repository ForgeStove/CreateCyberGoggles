package io.github.forgestove.create_cyber_goggles.core.overlay;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.*;

import java.lang.Math;
import java.util.*;
/**
 * Renders force arrows and a center-of-mass marker in-world for the currently
 * targeted sublevel.
 */
public final class ForceOverlayRenderer {
	private static final ResourceLocation GRAVITY_KEY = ResourceLocation.fromNamespaceAndPath("sable", "gravity");
	private static final double COM_HALF = 0.08;
	private static final double TAIL_SPHERE_PER_BBOX = 0.005;
	private static final double MAX_TAIL_SPHERE_RADIUS = 0.08;
	private static final double CONE_LEN_PER_LENGTH = 0.1;
	private static final double CONE_RADIUS_PER_LEN = 0.4;
	private static final double SHAFT_RADIUS_PER_CONE = 0.35;
	private static final double CONE_RADIUS_PER_TAIL = 1.5;
	private static final double SHAFT_RADIUS_PER_TAIL = 1.0;
	private ForceOverlayRenderer() {
	}
	/**
	 * Registered to {@link RenderLevelStageEvent} at {@link Stage#AFTER_LEVEL}
	 * .
	 */
	public static void onRenderStage(RenderLevelStageEvent event) {
		if (event.getStage() != Stage.AFTER_LEVEL) return;
		if (!CCG.config.aeronautics.forceOverlay.forceOverlayEnabled) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		var level = mc.level;
		if (player == null || level == null) return;
		var camera = event.getCamera();
		Matrix4fc modelViewMatrix = event.getModelViewMatrix();
		var bufferSource = mc.renderBuffers().bufferSource();
		var targetId = ForceOverlayClient.currentTarget();
		if (targetId == null) return;
		SubLevelContainer container = SubLevelContainer.getContainer(level);
		if (container == null) return;
		var raw = container.getSubLevel(targetId);
		if (!(raw instanceof ClientSubLevel clientSubLevel)) return;
		var renderPose = clientSubLevel.renderPose();
		var renderPos = renderPose.position();
		var rotationPoint = renderPose.rotationPoint();
		var camPos = camera.getPosition();
		var mvStack = RenderSystem.getModelViewStack();
		mvStack.pushMatrix();
		mvStack.set(modelViewMatrix);
		RenderSystem.applyModelViewMatrix();
		try {
			var poseStack = new PoseStack();
			// Translate to sublevel world position (camera-relative)
			poseStack.translate(renderPos.x() - camPos.x, renderPos.y() - camPos.y, renderPos.z() - camPos.z);
			// Apply sublevel rotation
			poseStack.mulPose(new Quaternionf(renderPose.orientation()));
			var scale = overlayPixelScale(mc, renderPos, camPos);
			var hasData = ForceOverlayClient.hasData();
			// Render center-of-mass marker (always, even without data)
			if (CCG.config.aeronautics.forceOverlay.renderCenterOfMass) {
				var fillType = OverlayRenderTypes.overlayFill();
				renderCenterOfMass(poseStack, bufferSource.getBuffer(fillType), hasData, scale);
				bufferSource.endBatch(fillType);
			}
			// Render force arrows
			if (hasData) renderForces(poseStack, bufferSource, rotationPoint, clientSubLevel, scale);
		} finally {
			mvStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
		}
	}
	private static void renderCenterOfMass(PoseStack poseStack, VertexConsumer consumer, boolean haveSnapshot, double scale) {
		float r, g, b;
		if (haveSnapshot) {
			r = 0.75f;
			g = 0.75f;
			b = 0.75f;
		} else {
			r = 1.0f;
			g = 1.0f;
			b = 1.0f;
		}
		quadCube(poseStack, consumer, COM_HALF * scale, r, g, b);
	}
	// ---- Force arrow rendering ----
	private static void renderForces(
		PoseStack poseStack,
		BufferSource bufferSource,
		Vector3dc rotationPoint,
		ClientSubLevel clientSubLevel,
		double scale
	) {
		var config = CCG.config.aeronautics.forceOverlay;
		var gravityFraction = config.gravityArrowFraction;
		var saturation = config.arrowSaturation;
		var minLen = config.minArrowLength;
		var clusters = ForceOverlayClient.smoothedClusters();
		if (clusters == null || clusters.isEmpty()) return;
		var gravityClusters = clusters.get(GRAVITY_KEY);
		if (gravityClusters == null || gravityClusters.isEmpty()) return;
		var gravityMagnitude = gravityClusters.getFirst().force().length();
		if (gravityMagnitude < 1e-6) return;
		var bbox = clientSubLevel.boundingBox();
		var bboxHeight = bbox.maxY() - bbox.minY();
		if (bboxHeight <= 0) return;
		var gravityArrowLen = bboxHeight * gravityFraction;
		var bboxMaxExtent = Math.max(bbox.maxX() - bbox.minX(), Math.max(bboxHeight, bbox.maxZ() - bbox.minZ()));
		var tailSphereRadius = Math.min(MAX_TAIL_SPHERE_RADIUS * scale, bboxMaxExtent * TAIL_SPHERE_PER_BBOX * scale);
		var maxConeRadius = tailSphereRadius * CONE_RADIUS_PER_TAIL;
		var maxShaftRadius = tailSphereRadius * SHAFT_RADIUS_PER_TAIL;
		var maxShapeLength = maxConeRadius / 0.04;
		// Build arrow draws
		List<ArrowDraw> arrows = new ArrayList<>();
		for (var entry : clusters.entrySet()) {
			var key = entry.getKey();
			if (!shouldShowForceGroup(key)) continue;
			var group = ForceGroups.REGISTRY.get(key);
			if (group == null) continue;
			var color = group.color();
			var r = (float) (color >> 16 & 0xFF) / 255.0f;
			var g = (float) (color >> 8 & 0xFF) / 255.0f;
			var b = (float) (color & 0xFF) / 255.0f;
			for (var cluster : entry.getValue()) {
				var arrow = buildArrow(
					cluster.pos(),
					cluster.force(),
					rotationPoint,
					gravityMagnitude,
					gravityArrowLen,
					saturation,
					minLen,
					r,
					g,
					b
				);
				if (arrow != null) arrows.add(arrow);
			}
		}
		if (arrows.isEmpty()) return;
		// Render all arrows
		var triType = OverlayRenderTypes.overlayTriangles();
		var triConsumer = bufferSource.getBuffer(triType);
		var pose = poseStack.last();
		for (var a : arrows) {
			var shapeLen = Math.min(a.length, maxShapeLength);
			var coneLen = Math.max(0.09, shapeLen * CONE_LEN_PER_LENGTH);
			var coneRadius = Math.min(maxConeRadius, coneLen * CONE_RADIUS_PER_LEN);
			var shaftRadius = Math.min(maxShaftRadius, coneRadius * SHAFT_RADIUS_PER_CONE);
			var shaftEndX = a.tx - a.dirX * coneLen;
			var shaftEndY = a.ty - a.dirY * coneLen;
			var shaftEndZ = a.tz - a.dirZ * coneLen;
			cylinder(pose, triConsumer, a.bx, a.by, a.bz, shaftEndX, shaftEndY, shaftEndZ, a.perp1, a.perp2, shaftRadius, a.r, a.g, a.b);
			cone(poseStack, triConsumer, a.tx, a.ty, a.tz, a.dirX, a.dirY, a.dirZ, a.perp1, a.perp2, coneLen, coneRadius, a.r, a.g, a.b);
			sphere(pose, triConsumer, a.bx, a.by, a.bz, tailSphereRadius, a.r, a.g, a.b);
		}
		bufferSource.endBatch(triType);
	}
	private static boolean shouldShowForceGroup(ResourceLocation key) {
		var namespace = key.getNamespace();
		var path = key.getPath();
		// Only filter sable force groups by config; unknown groups always show
		if (!"sable".equals(namespace)) return true;
		return switch (path) {
			case "gravity" -> CCG.config.aeronautics.forceOverlay.showGravity;
			case "drag" -> CCG.config.aeronautics.forceOverlay.showDrag;
			case "levitation" -> CCG.config.aeronautics.forceOverlay.showLevitation;
			case "balloon_lift" -> CCG.config.aeronautics.forceOverlay.showBalloonLift;
			case "propulsion" -> CCG.config.aeronautics.forceOverlay.showPropulsion;
			case "lift" -> CCG.config.aeronautics.forceOverlay.showLift;
			case "magnetic_force" -> CCG.config.aeronautics.forceOverlay.showMagneticForce;
			default -> true;
		};
	}
	// ---- Arrow geometry ----
	private static ArrowDraw buildArrow(
		Vector3dc forcePoint,
		Vector3dc forceVec,
		Vector3dc rotationPoint,
		double gravityMagnitude,
		double gravityArrowLen,
		double saturation,
		double minLen,
		float r,
		float g,
		float b
	) {
		var magnitude = forceVec.length();
		if (magnitude < 1e-6) return null;
		var ratio = magnitude / gravityMagnitude;
		// Saturating mapping so huge forces don't produce infinitely long arrows
		var visualRatio = ratio <= 1.0 ? ratio : saturation * ratio / (saturation + ratio - 1.0);
		var length = Math.max(minLen, visualRatio * gravityArrowLen);
		var dir = new Vector3d(forceVec).div(magnitude);
		// Force point in local sublevel coordinates (relative to rotation point)
		var bx = forcePoint.x() - rotationPoint.x();
		var by = forcePoint.y() - rotationPoint.y();
		var bz = forcePoint.z() - rotationPoint.z();
		var tx = bx + dir.x * length;
		var ty = by + dir.y * length;
		var tz = bz + dir.z * length;
		// Build perpendicular basis for cylinder/cone
		var ref = Math.abs(dir.y) < 0.9 ? new Vector3d(0, 1, 0) : new Vector3d(1, 0, 0);
		var perp1 = new Vector3d(dir).cross(ref).normalize();
		var perp2 = new Vector3d(dir).cross(perp1).normalize();
		return new ArrowDraw(bx, by, bz, tx, ty, tz, dir.x, dir.y, dir.z, perp1, perp2, length, r, g, b);
	}
	// ---- Primitive renderers ----
	private static void quadCube(PoseStack poseStack, VertexConsumer consumer, double half, float r, float g, float b) {
		var pose = poseStack.last();
		var n = (float) -half;
		var p = (float) half;
		// 6 quads for the cube faces
		quad(pose, consumer, n, n, n, n, p, n, p, p, n, p, n, n, r, g, b);
		quad(pose, consumer, p, n, p, p, p, p, p, p, n, p, n, n, r, g, b);
		quad(pose, consumer, n, n, p, p, n, p, p, n, n, n, n, n, r, g, b);
		quad(pose, consumer, n, p, n, p, p, n, p, p, p, n, p, p, r, g, b);
		quad(pose, consumer, p, n, n, p, p, n, n, p, n, n, n, n, r, g, b);
		quad(pose, consumer, n, n, p, n, p, p, p, p, p, p, n, p, r, g, b);
	}
	private static void quad(
		Pose pose,
		VertexConsumer consumer,
		float x1,
		float y1,
		float z1,
		float x2,
		float y2,
		float z2,
		float x3,
		float y3,
		float z3,
		float x4,
		float y4,
		float z4,
		float r,
		float g,
		float b
	) {
		consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, 1F);
		consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, 1F);
		consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, 1F);
		consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, 1F);
	}
	private static void cylinder(
		Pose pose,
		VertexConsumer consumer,
		double x0,
		double y0,
		double z0,
		double x1,
		double y1,
		double z1,
		Vector3d perp1,
		Vector3d perp2,
		double radius,
		float r,
		float g,
		float b
	) {
		for (var i = 0; i < 6; i++) {
			var a0 = Math.PI * 2.0 * i / 6;
			var a1 = Math.PI * 2.0 * (i + 1) / 6;
			var c0 = Math.cos(a0) * radius;
			var s0 = Math.sin(a0) * radius;
			var c1 = Math.cos(a1) * radius;
			var s1 = Math.sin(a1) * radius;
			var ox0 = perp1.x * c0 + perp2.x * s0;
			var oy0 = perp1.y * c0 + perp2.y * s0;
			var oz0 = perp1.z * c0 + perp2.z * s0;
			var ox1 = perp1.x * c1 + perp2.x * s1;
			var oy1 = perp1.y * c1 + perp2.y * s1;
			var oz1 = perp1.z * c1 + perp2.z * s1;
			triangle(pose, consumer, x0 + ox0, y0 + oy0, z0 + oz0, x1 + ox0, y1 + oy0, z1 + oz0, x1 + ox1, y1 + oy1, z1 + oz1, r, g, b);
			triangle(pose, consumer, x0 + ox0, y0 + oy0, z0 + oz0, x1 + ox1, y1 + oy1, z1 + oz1, x0 + ox1, y0 + oy1, z0 + oz1, r, g, b);
		}
	}
	private static void sphere(
		Pose pose,
		VertexConsumer consumer,
		double cx,
		double cy,
		double cz,
		double radius,
		float r,
		float g,
		float b
	) {
		var sinPhi = new double[4 + 1];
		var cosPhi = new double[4 + 1];
		for (var j = 0; j <= 4; j++) {
			var phi = -Math.PI / 2.0 + Math.PI * j / 4;
			sinPhi[j] = Math.sin(phi);
			cosPhi[j] = Math.cos(phi);
		}
		var sinTheta = new double[8 + 1];
		var cosTheta = new double[8 + 1];
		for (var i = 0; i <= 8; i++) {
			var theta = Math.PI * 2.0 * i / 8;
			sinTheta[i] = Math.sin(theta);
			cosTheta[i] = Math.cos(theta);
		}
		for (var j = 0; j < 4; j++) {
			var y0 = sinPhi[j] * radius;
			var y1 = sinPhi[j + 1] * radius;
			var r0 = cosPhi[j] * radius;
			var r1 = cosPhi[j + 1] * radius;
			for (var i = 0; i < 8; i++) {
				var x00 = cosTheta[i] * r0;
				var z00 = sinTheta[i] * r0;
				var x01 = cosTheta[i + 1] * r0;
				var z01 = sinTheta[i + 1] * r0;
				var x10 = cosTheta[i] * r1;
				var z10 = sinTheta[i] * r1;
				var x11 = cosTheta[i + 1] * r1;
				var z11 = sinTheta[i + 1] * r1;
				triangle(pose, consumer, cx + x00, cy + y0, cz + z00, cx + x10, cy + y1, cz + z10, cx + x11, cy + y1, cz + z11, r, g, b);
				triangle(pose, consumer, cx + x00, cy + y0, cz + z00, cx + x11, cy + y1, cz + z11, cx + x01, cy + y0, cz + z01, r, g, b);
			}
		}
	}
	private static void cone(
		PoseStack poseStack,
		VertexConsumer consumer,
		double tipX,
		double tipY,
		double tipZ,
		double dx,
		double dy,
		double dz,
		Vector3d perp1,
		Vector3d perp2,
		double length,
		double radius,
		float r,
		float g,
		float b
	) {
		var segments = 10;
		var baseX = tipX - dx * length;
		var baseY = tipY - dy * length;
		var baseZ = tipZ - dz * length;
		var cx = new double[segments + 1];
		var cy = new double[segments + 1];
		var cz = new double[segments + 1];
		for (var i = 0; i <= segments; i++) {
			var angle = Math.PI * 2.0 * i / segments;
			var c = Math.cos(angle);
			var s = Math.sin(angle);
			cx[i] = baseX + (perp1.x * c + perp2.x * s) * radius;
			cy[i] = baseY + (perp1.y * c + perp2.y * s) * radius;
			cz[i] = baseZ + (perp1.z * c + perp2.z * s) * radius;
		}
		var pose = poseStack.last();
		for (var i = 0; i < segments; i++) {
			triangle(pose, consumer, tipX, tipY, tipZ, cx[i], cy[i], cz[i], cx[i + 1], cy[i + 1], cz[i + 1], r, g, b);
			triangle(pose, consumer, baseX, baseY, baseZ, cx[i + 1], cy[i + 1], cz[i + 1], cx[i], cy[i], cz[i], r, g, b);
		}
	}
	private static void triangle(
		Pose pose,
		VertexConsumer consumer,
		double x1,
		double y1,
		double z1,
		double x2,
		double y2,
		double z2,
		double x3,
		double y3,
		double z3,
		float r,
		float g,
		float b
	) {
		consumer.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(r, g, b, 1.0f);
		consumer.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(r, g, b, 1.0f);
		consumer.addVertex(pose, (float) x3, (float) y3, (float) z3).setColor(r, g, b, 1.0f);
	}
	// ---- Utilities ----
	private static double overlayPixelScale(Minecraft mc, Vector3dc renderPos, Vec3 camPos) {
		var minPx = CCG.config.aeronautics.forceOverlay.minOverlayPixelSize;
		if (minPx <= 0) return 1.0;
		var viewportHeight = mc.getWindow().getHeight();
		if (viewportHeight <= 0) return 1.0;
		var dx = renderPos.x() - camPos.x;
		var dy = renderPos.y() - camPos.y;
		var dz = renderPos.z() - camPos.z;
		var distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance <= 0) return 1.0;
		int fovDeg = mc.options.fov().get();
		var worldPerPixel = 2.0 * distance * Math.tan(Math.toRadians(fovDeg) * 0.5) / viewportHeight;
		var minWorldEdge = minPx * worldPerPixel;
		var nominalEdge = 0.16;
		return Math.max(1.0, minWorldEdge / nominalEdge);
	}
	private record ArrowDraw(
		double bx,
		double by,
		double bz,
		double tx,
		double ty,
		double tz,
		double dirX,
		double dirY,
		double dirZ,
		Vector3d perp1,
		Vector3d perp2,
		double length,
		float r,
		float g,
		float b
	) {}
}
