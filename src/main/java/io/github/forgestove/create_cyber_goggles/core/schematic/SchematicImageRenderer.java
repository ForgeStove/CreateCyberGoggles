package io.github.forgestove.create_cyber_goggles.core.schematic;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.simibubi.create.content.schematics.client.SchematicRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;
import java.util.*;
/**
 * 把一份蓝图离屏渲染成 {@link NativeImage}：正交投影 + 按包围盒自适应取景 + 流体补渲。
 * <p>
 * 移植自 Create: Blueprinted（MIT）。
 */
public final class SchematicImageRenderer {
	private static final int MARGIN_PX = 4;
	private static final int MAX_FB_SIZE = 16384;
	private static final float DEFAULT_NEAR_AND_FAR = 10_000F;
	private final SchematicLevel schematicLevel;
	private final List<BlockPos> fluidPositions;
	private final PoseAppliedVertexConsumer fluidConsumer;
	private RenderTarget renderTarget;
	private int targetW, targetH;
	private SchematicImageRenderer(SchematicLevel schematicLevel, List<BlockPos> fluidPositions) {
		this.schematicLevel = schematicLevel;
		this.fluidPositions = fluidPositions;
		fluidConsumer = new PoseAppliedVertexConsumer();
	}
	public static Optional<SchematicImageRenderer> bakeFromTemplate(
		StructureTemplate template,
		SchematicLevel schematicLevel,
		Level level
	) {
		template.placeInWorld(
			schematicLevel,
			BlockPos.ZERO,
			BlockPos.ZERO,
			new StructurePlaceSettings(),
			schematicLevel.random,
			Block.UPDATE_CLIENTS
		);
		// sable 的子维度不在这份模板的方块表里，得单独展开成渲染层级，否则图里它们是空的
		SchematicImageUtil.attachSubLevels(template, schematicLevel, level);
		// 不能因为 placeInWorld 返回 false 就当成空图：sable 的飞船整艘都是子维度，
		// 主模板里一个方块都没有，内容全在子层级里
		if (schematicLevel.getBlockMap().isEmpty() && !SchematicImageUtil.hasSubLevels(schematicLevel)) return Optional.empty();
		var fluidPositions = new ArrayList<BlockPos>();
		for (var blockEntry : schematicLevel.getBlockMap().entrySet()) {
			var pos = blockEntry.getKey();
			var state = blockEntry.getValue();
			if (!state.isAir() && !state.getFluidState().isEmpty()) fluidPositions.add(pos.immutable());
		}
		return Optional.of(new SchematicImageRenderer(schematicLevel, fluidPositions));
	}
	/** 按 factor×factor 方块做 alpha 加权平均降采样（超采样抗锯齿） */
	public static NativeImage downsample(NativeImage source, int factor) {
		try (source) {
			var outW = Math.max(1, source.getWidth() / factor);
			var outH = Math.max(1, source.getHeight() / factor);
			var out = new NativeImage(outW, outH, false);
			var samples = factor * factor;
			try {
				for (var y = 0; y < outH; y++)
					for (var x = 0; x < outW; x++) {
						long aSum = 0, c0 = 0, c1 = 0, c2 = 0;
						for (var dy = 0; dy < factor; dy++)
							for (var dx = 0; dx < factor; dx++) {
								var p = source.getPixelRGBA(x * factor + dx, y * factor + dy);
								var a = p >>> 24 & 0xFF;
								aSum += a;
								c0 += (long) (p & 0xFF) * a;
								c1 += (long) (p >> 8 & 0xFF) * a;
								c2 += (long) (p >> 16 & 0xFF) * a;
							}
						var a = (int) (aSum / samples);
						var r0 = aSum == 0 ? 0 : (int) (c0 / aSum);
						var r1 = aSum == 0 ? 0 : (int) (c1 / aSum);
						var r2 = aSum == 0 ? 0 : (int) (c2 / aSum);
						out.setPixelRGBA(x, y, a << 24 | r2 << 16 | r1 << 8 | r0);
					}
				return out;
			} catch (IllegalArgumentException e) {
				out.close();
				throw new SchematicImageRenderException("Failed to downsample ", e);
			}
		}
	}
	public @NotNull NativeImage render(SchematicRenderSettings settings) {
		RenderSystem.assertOnRenderThread();
		BoundingBox bounds = SchematicImageUtil.frameBounds(schematicLevel);
		float minX = bounds.minX(), minY = bounds.minY(), minZ = bounds.minZ();
		float maxX = bounds.maxX() + 1F, maxY = bounds.maxY() + 1F, maxZ = bounds.maxZ() + 1F;
		float centerX = (minX + maxX) / 2F, centreY = (minY + maxY) / 2F, centreZ = (minZ + maxZ) / 2F;
		float hx = (maxX - minX) / 2F, hy = (maxY - minY) / 2F, hz = (maxZ - minZ) / 2F;
		var rot = new Matrix4f();
		var orientation = settings.orientation();
		rot.rotateX((float) Math.toRadians(orientation.pitch()));
		rot.rotateY((float) Math.toRadians(orientation.yaw()));
		rot.rotateZ((float) Math.toRadians(orientation.roll()));
		// 旋转后 8 个包围盒角点在屏幕平面上的最大投影，决定取景尺寸
		float maxPX = 0F, maxPY = 0F;
		for (var sx = -1; sx <= 1; sx += 2)
			for (var sy = -1; sy <= 1; sy += 2)
				for (var sz = -1; sz <= 1; sz += 2) {
					var v = rot.transformPosition(new Vector3f(sx * hx, sy * hy, sz * hz));
					maxPX = Math.max(maxPX, Math.abs(v.x()));
					maxPY = Math.max(maxPY, Math.abs(v.y()));
				}
		var projW = Math.max(1e-3F, 2F * maxPX);
		var projH = Math.max(1e-3F, 2F * maxPY);
		var targetWidth = settings.imageWidth();
		var scale = Math.max(1, targetWidth - MARGIN_PX * 2) / projW;
		var needW = Math.round(projW * scale) + MARGIN_PX * 2;
		var needH = Math.round(projH * scale) + MARGIN_PX * 2;
		return renderCore(settings, scale, needW, needH, new Vector3f(centerX, centreY, centreZ));
	}
	private @NotNull NativeImage renderCore(SchematicRenderSettings settings, float scale, int needW, int needH, Vector3f centerPos) {
		var mc = Minecraft.getInstance();
		var maxSupported = Math.min(MAX_FB_SIZE, RenderSystem.maxSupportedTextureSize());
		if (needW > maxSupported || needH > maxSupported) {
			var cap = (float) maxSupported / Math.max(needW, needH);
			scale *= cap;
			needW = Math.min(needW, maxSupported);
			needH = Math.min(needH, maxSupported);
		}
		ensureTarget(needW, needH);
		var prevProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
		var prevSorting = RenderSystem.getVertexSorting();
		try {
			var bg = unpackArgb(settings.backgroundColor().getRGB());
			renderTarget.setClearColor(bg[0], bg[1], bg[2], bg[3]);
			renderTarget.clear(Minecraft.ON_OSX);
			renderTarget.bindWrite(true);
			var projection = new Matrix4f().setOrtho(
				-targetW / 2F,
				targetW / 2F,
				-targetH / 2F,
				targetH / 2F,
				-DEFAULT_NEAR_AND_FAR,
				DEFAULT_NEAR_AND_FAR
			);
			RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);
			var poseStack = new PoseStack();
			poseStack.pushPose();
			RenderSystem.enableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			Lighting.setupLevel();
			poseStack.scale(scale, scale, scale);
			var orientation = settings.orientation();
			poseStack.mulPose(Axis.XP.rotationDegrees(orientation.pitch()));
			poseStack.mulPose(Axis.YP.rotationDegrees(orientation.yaw()));
			if (orientation.roll() != 0F) poseStack.mulPose(Axis.ZP.rotationDegrees(orientation.roll()));
			poseStack.translate(-centerPos.x, -centerPos.y, -centerPos.z);
			SuperRenderTypeBuffer buffers = DefaultSuperRenderTypeBuffer.getInstance();
			var renderer = new SchematicRenderer(schematicLevel);
			renderer.render(poseStack, buffers);
			renderFluids(poseStack, buffers);
			buffers.draw();
			poseStack.popPose();
			NativeImage image = null;
			try {
				image = new NativeImage(targetW, targetH, false);
				RenderSystem.bindTexture(renderTarget.getColorTextureId());
				image.downloadTexture(0, false);
				image.flipY();
				return image;
			} catch (Exception e) {
				if (image != null) image.close();
				throw new SchematicImageRenderException("Failed to download rendered schematic image", e);
			}
		} catch (IllegalStateException | NullPointerException e) {
			throw new SchematicImageRenderException("Failed to render schematic", e);
		} finally {
			try {
				if (renderTarget != null) renderTarget.destroyBuffers();
			} catch (Exception e) {
				CCG.LOGGER.error("Failed to destroy render target buffers", e);
			}
			mc.getMainRenderTarget().bindWrite(true);
			RenderSystem.setProjectionMatrix(prevProjection, prevSorting);
		}
	}
	private void ensureTarget(int needW, int needH) {
		if (renderTarget != null && targetW >= needW && targetH >= needH) return;
		var newW = Math.max(targetW, needW);
		var newH = Math.max(targetH, needH);
		if (renderTarget != null) renderTarget.destroyBuffers();
		renderTarget = new TextureTarget(newW, newH, true, Minecraft.ON_OSX);
		targetW = newW;
		targetH = newH;
	}
	private static float[] unpackArgb(int argb) {
		var a = (argb >> 24 & 0xFF) / 255F;
		var r = (argb >> 16 & 0xFF) / 255F;
		var g = (argb >> 8 & 0xFF) / 255F;
		var b = (argb & 0xFF) / 255F;
		return new float[]{r, g, b, a};
	}
	/** Create 的蓝图渲染器会跳过流体，这里按位置补渲一遍 */
	private void renderFluids(PoseStack poseStack, SuperRenderTypeBuffer buffers) {
		if (fluidPositions.isEmpty()) return;
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		Matrix4f pose = poseStack.last().pose();
		Matrix3f normal = poseStack.last().normal();
		for (var pos : fluidPositions) {
			BlockState state = schematicLevel.getBlockState(pos);
			FluidState fluid = state.getFluidState();
			if (fluid.isEmpty()) continue;
			RenderType layer = ItemBlockRenderTypes.getRenderLayer(fluid);
			// 流体模型坐标取区块内局部坐标，这里把区块原点补上
			fluidConsumer.prepare(
				buffers.getBuffer(layer),
				pose,
				normal,
				pos.getX() - (pos.getX() & 15),
				pos.getY() - (pos.getY() & 15),
				pos.getZ() - (pos.getZ() & 15)
			);
			dispatcher.renderLiquid(pos, schematicLevel, fluidConsumer, state, fluid);
		}
	}
}
