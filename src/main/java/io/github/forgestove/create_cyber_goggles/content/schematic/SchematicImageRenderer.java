package io.github.forgestove.create_cyber_goggles.content.schematic;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.GlStateManager.*;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.simibubi.create.content.schematics.client.SchematicRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.content.schematic.SchematicRenderSettings.Orientation;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
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
import java.util.Map.Entry;
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
			RandomSource.create(),
			Block.UPDATE_CLIENTS
		);
		SchematicImageUtil.attachSubLevels(template, schematicLevel, level);
		if (schematicLevel.getBlockMap().isEmpty() && !SchematicImageUtil.hasSubLevels(schematicLevel)) return Optional.empty();
		var fluidPositions = new ArrayList<BlockPos>();
		for (Entry<BlockPos, BlockState> blockEntry : schematicLevel.getBlockMap().entrySet()) {
			BlockPos pos = blockEntry.getKey();
			BlockState state = blockEntry.getValue();
			if (!state.isAir() && !state.getFluidState().isEmpty()) fluidPositions.add(pos.immutable());
		}
		return Optional.of(new SchematicImageRenderer(schematicLevel, fluidPositions));
	}
	public static NativeImage downsample(NativeImage source, int factor) {
		try (source) {
			int outW = Math.max(1, source.getWidth() / factor);
			int outH = Math.max(1, source.getHeight() / factor);
			var out = new NativeImage(outW, outH, false);
			int samples = factor * factor;
			try {
				for (var y = 0; y < outH; y++)
					for (var x = 0; x < outW; x++) {
						long aSum = 0, c0 = 0, c1 = 0, c2 = 0;
						for (var dy = 0; dy < factor; dy++)
							for (var dx = 0; dx < factor; dx++) {
								int p = source.getPixelRGBA(x * factor + dx, y * factor + dy);
								int a = p >>> 24 & 0xFF;
								aSum += a;
								c0 += (long) (p & 0xFF) * a;
								c1 += (long) (p >> 8 & 0xFF) * a;
								c2 += (long) (p >> 16 & 0xFF) * a;
							}
						var a = (int) (aSum / samples);
						int r0 = aSum == 0 ? 0 : (int) (c0 / aSum);
						int r1 = aSum == 0 ? 0 : (int) (c1 / aSum);
						int r2 = aSum == 0 ? 0 : (int) (c2 / aSum);
						out.setPixelRGBA(x, y, a << 24 | r2 << 16 | r1 << 8 | r0);
					}
				return out;
			} catch (IllegalArgumentException e) {
				out.close();
				throw new SchematicImageRenderException("Failed to downsample ", e);
			}
		}
	}
	private static float[] unpackArgb(int argb) {
		float a = (argb >> 24 & 0xFF) / 255F;
		float r = (argb >> 16 & 0xFF) / 255F;
		float g = (argb >> 8 & 0xFF) / 255F;
		float b = (argb & 0xFF) / 255F;
		return new float[]{r, g, b, a};
	}
	public @NotNull NativeImage render(SchematicRenderSettings settings) {
		RenderSystem.assertOnRenderThread();
		BoundingBox bounds = SchematicImageUtil.frameBounds(schematicLevel);
		float minX = bounds.minX(), minY = bounds.minY(), minZ = bounds.minZ();
		float maxX = bounds.maxX() + 1F, maxY = bounds.maxY() + 1F, maxZ = bounds.maxZ() + 1F;
		float centerX = (minX + maxX) / 2F, centreY = (minY + maxY) / 2F, centreZ = (minZ + maxZ) / 2F;
		float hx = (maxX - minX) / 2F, hy = (maxY - minY) / 2F, hz = (maxZ - minZ) / 2F;
		var rot = new Matrix4f();
		Orientation orientation = settings.orientation();
		rot.rotateX((float) Math.toRadians(orientation.pitch()));
		rot.rotateY((float) Math.toRadians(orientation.yaw()));
		rot.rotateZ((float) Math.toRadians(orientation.roll()));
		float maxPX = 0F, maxPY = 0F;
		for (int sx = -1; sx <= 1; sx += 2)
			for (int sy = -1; sy <= 1; sy += 2)
				for (int sz = -1; sz <= 1; sz += 2) {
					Vector3f v = rot.transformPosition(new Vector3f(sx * hx, sy * hy, sz * hz));
					maxPX = Math.max(maxPX, Math.abs(v.x()));
					maxPY = Math.max(maxPY, Math.abs(v.y()));
				}
		float projW = Math.max(1e-3F, 2F * maxPX);
		float projH = Math.max(1e-3F, 2F * maxPY);
		int targetWidth = settings.imageWidth();
		float scale = Math.max(1, targetWidth - MARGIN_PX * 2) / projW;
		int needW = Math.round(projW * scale) + MARGIN_PX * 2;
		int needH = Math.round(projH * scale) + MARGIN_PX * 2;
		return renderCore(settings, scale, needW, needH, new Vector3f(centerX, centreY, centreZ));
	}
	private @NotNull NativeImage renderCore(SchematicRenderSettings settings, float scale, int needW, int needH, Vector3f centerPos) {
		Minecraft mc = Minecraft.getInstance();
		int maxSupported = Math.min(MAX_FB_SIZE, RenderSystem.maxSupportedTextureSize());
		if (needW > maxSupported || needH > maxSupported) {
			float cap = (float) maxSupported / Math.max(needW, needH);
			scale *= cap;
			needW = Math.min(needW, maxSupported);
			needH = Math.min(needH, maxSupported);
		}
		ensureTarget(needW, needH);
		var prevProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
		VertexSorting prevSorting = RenderSystem.getVertexSorting();
		try {
			float[] bg = unpackArgb(settings.backgroundColor().getRGB());
			renderTarget.setClearColor(bg[0], bg[1], bg[2], bg[3]);
			renderTarget.clear(Minecraft.ON_OSX);
			renderTarget.bindWrite(true);
			Matrix4f projection = new Matrix4f().setOrtho(
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
			Orientation orientation = settings.orientation();
			poseStack.mulPose(Axis.XP.rotationDegrees(orientation.pitch()));
			poseStack.mulPose(Axis.YP.rotationDegrees(orientation.yaw()));
			if (orientation.roll() != 0F) poseStack.mulPose(Axis.ZP.rotationDegrees(orientation.roll()));
			poseStack.translate(-centerPos.x, -centerPos.y, -centerPos.z);
			SuperRenderTypeBuffer buffers = DefaultSuperRenderTypeBuffer.getInstance();
			var renderer = new SchematicRenderer(schematicLevel);
			renderer.render(poseStack, buffers);
			SchematicImageUtil.renderSubLevelBlockEntities(poseStack, buffers, schematicLevel);
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
		int newW = Math.max(targetW, needW);
		int newH = Math.max(targetH, needH);
		if (renderTarget != null) renderTarget.destroyBuffers();
		renderTarget = new TextureTarget(newW, newH, true, Minecraft.ON_OSX);
		targetW = newW;
		targetH = newH;
	}
	private void renderFluids(PoseStack poseStack, SuperRenderTypeBuffer buffers) {
		if (fluidPositions.isEmpty()) return;
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		Matrix4f pose = poseStack.last().pose();
		Matrix3f normal = poseStack.last().normal();
		for (BlockPos pos : fluidPositions) {
			BlockState state = schematicLevel.getBlockState(pos);
			FluidState fluid = state.getFluidState();
			if (fluid.isEmpty()) continue;
			RenderType layer = ItemBlockRenderTypes.getRenderLayer(fluid);
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
