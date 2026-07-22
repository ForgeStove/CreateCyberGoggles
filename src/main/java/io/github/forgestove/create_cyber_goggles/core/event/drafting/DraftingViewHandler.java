package io.github.forgestove.create_cyber_goggles.core.event.drafting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Vector3f;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
/**
 * 对主渲染目标应用绘图视图后期处理效果。
 * <p>
 * 该效果执行：通过深度缓冲进行边缘检测、基于调色板的颜色量化与抖动、可选像素化，以及类似墨水在纸上的外观。
 */
public final class DraftingViewHandler {
	private static final ResourceLocation PALETTE_TEXTURE = getCCGRes("textures/effects/diagram_palette.png");
	private static final ResourceLocation DITHER_TEXTURE = getCCGRes("textures/effects/dither.png");
	private static DraftingFramebuffer framebuffer;
	/**
	 * 在所有方块/实体几何体渲染完成后、Create 渲染示意图箭头及其他覆盖元素
	 * {@link Stage#AFTER_PARTICLES} 之前应用绘图视图效果，使它们在风格化场景上方保持清晰。
	 */
	@SuppressWarnings("resource")
	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS) return;
		if (!CCG.config.overlay.draftingView.draftingViewEnabled) return;
		var view = DraftingShaders.draftingView();
		var upscale = DraftingShaders.draftingUpscale();
		if (view == null || upscale == null) return;
		var main = mc.getMainRenderTarget();
		var window = mc.getWindow();
		if (framebuffer == null) framebuffer = new DraftingFramebuffer(main.width, main.height);
		else framebuffer.resizeIfNeeded(main.width, main.height);
		var cfg = CCG.config.overlay.draftingView;
		var lineColor = unpackColor(cfg.lineColor);
		var lineShadow = unpackColor(cfg.lineShadowColor);
		var paletteOffset = (float) cfg.paletteOffset;
		var pixelate = cfg.pixelate;
		var pixelScale = (float) cfg.pixelScale;
		var palette = mc.getTextureManager().getTexture(PALETTE_TEXTURE);
		var dither = mc.getTextureManager().getTexture(DITHER_TEXTURE);
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		// 第一遍：将主场景通过绘图视图着色器渲染到离屏缓冲
		framebuffer.target.bindWrite(true);
		view.setSampler("DiffuseSampler0", main.getColorTextureId());
		view.setSampler("DiffuseDepthSampler", main.getDepthTextureId());
		view.setSampler("Palette", palette);
		view.setSampler("Dither", dither);
		view.safeGetUniform("LineColor").set(lineColor.x, lineColor.y, lineColor.z, 1.0f);
		view.safeGetUniform("LineShadowColor").set(lineShadow.x, lineShadow.y, lineShadow.z, 1.0f);
		view.safeGetUniform("InSize").set((float) window.getWidth(), (float) window.getHeight());
		view.safeGetUniform("PaletteOffset").set(paletteOffset);
		view.safeGetUniform("Pixelate").set(pixelate ? 1.0f : 0.0f);
		view.safeGetUniform("PixelScale").set(pixelScale);
		RenderSystem.setShader(() -> view);
		drawFullscreenTriangle();
		view.clear();
		// 第二遍：将离屏缓冲复制回主渲染目标（最近邻上采样）
		main.bindWrite(true);
		upscale.setSampler("DiffuseSampler0", framebuffer.target.getColorTextureId());
		RenderSystem.setShader(() -> upscale);
		drawFullscreenTriangle();
		upscale.clear();
		RenderSystem.depthMask(true);
	}
	private static Vector3f unpackColor(int argb) {
		return new Vector3f((float) (argb >> 16 & 0xFF) / 255.0f, (float) (argb >> 8 & 0xFF) / 255.0f, (float) (argb & 0xFF) / 255.0f);
	}
	private static void drawFullscreenTriangle() {
		var tess = Tesselator.getInstance();
		var bb = tess.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION);
		bb.addVertex(-1.0f, -1.0f, 0.0f);
		bb.addVertex(3.0f, -1.0f, 0.0f);
		bb.addVertex(-1.0f, 3.0f, 0.0f);
		var mesh = bb.buildOrThrow();
		BufferUploader.drawWithShader(mesh);
	}
}
