package io.github.forgestove.create_cyber_goggles.core.event.forceOverlay;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.CCGRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.*;

import java.lang.Math;
import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
/**
 * 为当前目标子层级在世界中渲染力箭头和质心标记。
 */
public final class ForceOverlayRenderer {
	private static final ResourceLocation GRAVITY_KEY = getRes("sable", "gravity");
	private static final double COM_HALF = 0.08, TAIL_SPHERE_PER_BBOX = 0.005, MAX_TAIL_SPHERE_RADIUS = 0.08, CONE_LEN_PER_LENGTH = 0.1,
		CONE_RADIUS_PER_LEN = 0.4, SHAFT_RADIUS_PER_CONE = 0.35, CONE_RADIUS_PER_TAIL = 1.5, SHAFT_RADIUS_PER_TAIL = 1.0;
	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != Stage.AFTER_LEVEL) return;
		if (!CCG.config.aeronautics.forceOverlay.enableForceOverlay) return;
		var player = mc.player;
		var level = mc.level;
		if (player == null || level == null) return;
		var camera = event.getCamera();
		Matrix4fc modelViewMatrix = event.getModelViewMatrix();
		var bufferSource = mc.renderBuffers().bufferSource();
		var targetId = ForceOverlay.currentTarget();
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
			// 平移至子层级世界位置（相对于相机）
			poseStack.translate(renderPos.x() - camPos.x, renderPos.y() - camPos.y, renderPos.z() - camPos.z);
			// 应用子层级旋转
			poseStack.mulPose(new Quaternionf(renderPose.orientation()));
			var scale = overlayPixelScale(renderPos, camPos);
			var hasData = ForceOverlay.hasData();
			// 渲染质心标记（始终渲染，即使没有数据）
			if (CCG.config.aeronautics.forceOverlay.showCenterOfMass) {
				var fillType = CCGRenderTypes.OVERLAY_FILL;
				var consumer = bufferSource.getBuffer(fillType);
				float r, g, b;
				if (hasData) {
					r = 0.75f;
					g = 0.75f;
					b = 0.75f;
				} else {
					r = 1.0f;
					g = 1.0f;
					b = 1.0f;
				}
				quadCube(poseStack, consumer, COM_HALF * scale, r, g, b);
				bufferSource.endBatch(fillType);
			}
			// 渲染力箭头
			if (hasData) {
				renderForces(poseStack, bufferSource, rotationPoint, clientSubLevel, scale);
				renderWorldLabels(poseStack, bufferSource, rotationPoint, clientSubLevel, scale, camera);
			}
		} finally {
			mvStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
		}
	}
	// ---- 工具方法 ----
	private static double overlayPixelScale(Vector3dc renderPos, Vec3 camPos) {
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
	// ---- 图元渲染器 ----
	private static void quadCube(PoseStack poseStack, VertexConsumer consumer, double half, float r, float g, float b) {
		var pose = poseStack.last();
		var n = (float) -half;
		var p = (float) half;
		// 立方体六个面的四边形
		quad(pose, consumer, n, n, n, n, p, n, p, p, n, p, n, n, r, g, b);
		quad(pose, consumer, p, n, p, p, p, p, p, p, n, p, n, n, r, g, b);
		quad(pose, consumer, n, n, p, p, n, p, p, n, n, n, n, n, r, g, b);
		quad(pose, consumer, n, p, n, p, p, n, p, p, p, n, p, p, r, g, b);
		quad(pose, consumer, p, n, n, p, p, n, n, p, n, n, n, n, r, g, b);
		quad(pose, consumer, n, n, p, n, p, p, p, p, p, p, n, p, r, g, b);
	}
	// ---- 力箭头渲染 ----
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
		var clusters = ForceOverlay.smoothedClusters();
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
		// 构建箭头绘制
		List<ArrowDraw> arrows = new ArrayList<>();
		clusters.forEach((key, value) -> {
			if (!shouldShowForceGroup(key)) return;
			var group = ForceGroups.REGISTRY.get(key);
			if (group == null) return;
			var color = group.color();
			var r = (float) (color >> 16 & 0xFF) / 255.0f;
			var g = (float) (color >> 8 & 0xFF) / 255.0f;
			var b = (float) (color & 0xFF) / 255.0f;
			for (var cluster : value) {
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
		});
		if (arrows.isEmpty()) return;
		// 渲染所有箭头
		var triType = CCGRenderTypes.OVERLAY_TRIANGLES;
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
	// ---- 世界标签渲染 ----
	/** 收集所有簇的世界坐标 → 在独立世界空间渲染广告牌文字。 */
	private static void renderWorldLabels(
		PoseStack poseStack,
		BufferSource bufferSource,
		Vector3dc rotationPoint,
		ClientSubLevel clientSubLevel,
		double scale,
		Camera camera
	) {
		var config = CCG.config.aeronautics.forceOverlay;
		if (!config.useWorldLabels) return;
		var clusters = ForceOverlay.smoothedClusters();
		if (clusters == null || clusters.isEmpty()) return;
		var font = mc.font;
		var camPos = camera.getPosition();
		var renderPose = clientSubLevel.renderPose();
		var subPos = renderPose.position();
		var subOrient = new Quaternionf(renderPose.orientation());
		var bbox = clientSubLevel.boundingBox();
		var labelOffsetY = (bbox.maxY() - bbox.minY()) * 0.015;
		// 保存子层级变换、重置为世界空间
		poseStack.pushPose();
		poseStack.last().pose().identity();
		poseStack.last().normal().identity();
		var textScaleF = (float) (0.01 * scale * config.worldLabelScale);
		// 采集：计算每个标签的世界坐标
		record Entry(Component text, Vector3d worldPos) {}
		List<Entry> entries = new ArrayList<>();
		clusters.forEach((key, value) -> {
			if (!shouldShowForceGroup(key)) return;
			var group = ForceGroups.REGISTRY.get(key);
			if (group == null) return;
			var groupColor = group.color();
			for (var cluster : value) {
				var mag = cluster.force().length();
				if (mag < 1e-6) continue;
				var groupName = group.name().getString();
				var valueStr = String.format(Locale.ROOT, "%.1f", mag);
				var text = Component.empty()
					.append(Component.literal(groupName).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(groupColor))))
					.append(Component.literal("：").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xB0B0B0))))
					.append(Component.literal(valueStr + " N").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))));
				var forceDir = new Vector3d(cluster.force()).div(mag);
				var local = new Vector3d(
					cluster.pos().x() - rotationPoint.x() - forceDir.x * 0.5,
					cluster.pos().y() - rotationPoint.y() + labelOffsetY - forceDir.y * 0.5,
					cluster.pos().z() - rotationPoint.z() - forceDir.z * 0.5
				);
				var worldDir = subOrient.transform(new Vector3d(local));
				entries.add(new Entry(
					text,
					new Vector3d(
						subPos.x() + worldDir.x - camPos.x(),
						subPos.y() + worldDir.y - camPos.y(),
						subPos.z() + worldDir.z - camPos.z()
					)
				));
			}
		});
		if (entries.isEmpty()) {
			poseStack.popPose();
			return;
		}
		var cameraRot = new Quaternionf(camera.rotation());
		// 背景
		var fillConsumer = bufferSource.getBuffer(CCGRenderTypes.OVERLAY_FILL);
		for (var e : entries) {
			var halfW = font.width(e.text) / 2f;
			poseStack.pushPose();
			poseStack.translate(e.worldPos.x(), e.worldPos.y(), e.worldPos.z());
			poseStack.mulPose(cameraRot);
			poseStack.scale(textScaleF, -textScaleF, textScaleF);
			drawLabelBg(poseStack.last(), fillConsumer, halfW);
			poseStack.popPose();
		}
		bufferSource.endBatch(CCGRenderTypes.OVERLAY_FILL);
		// 文字
		RenderSystem.disableCull();
		for (var e : entries) {
			var halfW = font.width(e.text) / 2;
			poseStack.pushPose();
			poseStack.translate(e.worldPos.x(), e.worldPos.y(), e.worldPos.z());
			poseStack.mulPose(cameraRot);
			poseStack.scale(textScaleF, -textScaleF, textScaleF);
			font.drawInBatch(
				e.text,
				-halfW,
				1,
				0xFFFFFF,
				false,
				poseStack.last().pose(),
				bufferSource,
				DisplayMode.SEE_THROUGH,
				0xF000F0,
				0
			);
			poseStack.popPose();
		}
		bufferSource.endBatch();
		RenderSystem.enableCull();
		poseStack.popPose(); // 恢复子层级变换
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
	private static boolean shouldShowForceGroup(ResourceLocation key) {
		var namespace = key.getNamespace();
		var path = key.getPath();
		// 仅根据配置过滤 sable 力组；未知组始终显示
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
	// ---- 箭头几何 ----
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
		// 饱和映射，使巨大力量不会产生无限长的箭头
		var visualRatio = ratio <= 1.0 ? ratio : saturation * ratio / (saturation + ratio - 1.0);
		var length = Math.max(minLen, visualRatio * gravityArrowLen);
		var dir = new Vector3d(forceVec).div(magnitude);
		// 力点在局部子层级坐标中（相对于旋转点）
		var bx = forcePoint.x() - rotationPoint.x();
		var by = forcePoint.y() - rotationPoint.y();
		var bz = forcePoint.z() - rotationPoint.z();
		var tx = bx + dir.x * length;
		var ty = by + dir.y * length;
		var tz = bz + dir.z * length;
		// 构建圆柱体/圆锥体的垂直基向量
		var ref = Math.abs(dir.y) < 0.9 ? new Vector3d(0, 1, 0) : new Vector3d(1, 0, 0);
		var perp1 = new Vector3d(dir).cross(ref).normalize();
		var perp2 = new Vector3d(dir).cross(perp1).normalize();
		return new ArrowDraw(bx, by, bz, tx, ty, tz, dir.x, dir.y, dir.z, perp1, perp2, length, r, g, b);
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
	/** 绘制面板风格的背景 + 边框，与 Create GUI 面板样式一致（四角空、左/右边框上下各缩 1px）。 */
	private static void drawLabelBg(Pose pose, VertexConsumer consumer, float halfW) {
		var m = pose.pose();
		var x = -halfW;
		var width = 2 * halfW;
		var y = 1f;
		var height = 9f;
		// 颜色参照 GUI：BG=0xC6C6C6, DARK=0x555555
		int fillRgb = 0x101010, fillA = 0xB0;
		int bdrRgb = 0x505050, bdrA = 0xE0;
		// 填充区域（四角留空）
		coloredQuad(m, consumer, x - 3, y - 4, x + width + 3, y - 3, fillRgb, fillA);
		coloredQuad(m, consumer, x - 3, y + height + 3, x + width + 3, y + height + 4, fillRgb, fillA);
		coloredQuad(m, consumer, x - 3, y - 3, x + width + 3, y + height + 3, fillRgb, fillA);
		coloredQuad(m, consumer, x - 4, y - 3, x - 3, y + height + 3, fillRgb, fillA);
		coloredQuad(m, consumer, x + width + 3, y - 3, x + width + 4, y + height + 3, fillRgb, fillA);
		// 边框（统一使用 DARK，top 和 bot 同色）
		coloredQuad(m, consumer, x - 3, y - 2, x - 2, y + height + 2, bdrRgb, bdrA);   // 左
		coloredQuad(m, consumer, x + width + 2, y - 2, x + width + 3, y + height + 2, bdrRgb, bdrA); // 右
		coloredQuad(m, consumer, x - 3, y - 3, x + width + 3, y - 2, bdrRgb, bdrA);     // 上
		coloredQuad(m, consumer, x - 3, y + height + 2, x + width + 3, y + height + 3, bdrRgb, bdrA); // 下
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
	/** 绘制单个同色矩形（用于面板边框）。 */
	private static void coloredQuad(Matrix4f m, VertexConsumer consumer, float x1, float y1, float x2, float y2, int rgb, int alpha) {
		float r = (rgb >> 16 & 0xFF) / 255f, g = (rgb >> 8 & 0xFF) / 255f, b = (rgb & 0xFF) / 255f, a = alpha / 255f;
		consumer.addVertex(m, x1, y1, 0).setColor(r, g, b, a);
		consumer.addVertex(m, x2, y1, 0).setColor(r, g, b, a);
		consumer.addVertex(m, x2, y2, 0).setColor(r, g, b, a);
		consumer.addVertex(m, x1, y2, 0).setColor(r, g, b, a);
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
