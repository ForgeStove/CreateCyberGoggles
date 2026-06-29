package io.github.forgestove.create_cyber_goggles.core.overlay.drafting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Vector3f;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getCCGRes;
/**
 * Applies the drafting-view post-processing effect to the main render target.
 * <p>
 * The effect performs: edge detection via depth-buffer, palette-based color
 * quantization with dithering, optional pixelation, and an ink-on-paper look.
 */
public final class DraftingViewHandler {
	private static final ResourceLocation PALETTE_TEXTURE = getCCGRes("textures/effects/diagram_palette.png");
	private static final ResourceLocation DITHER_TEXTURE = getCCGRes("textures/effects/dither.png");
	private static DraftingFramebuffer framebuffer;
	/**
	 * Applies the drafting-view effect after all block/entity geometry is done but
	 * {@linkplain Stage#AFTER_PARTICLES before} Create renders schematic arrows
	 * and other overlay elements so they stay crisp on top of the styled scene.
	 */
	public static void applyIfEnabled(RenderLevelStageEvent event) {
		if (event.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS) return;
		if (!CCG.config.draftingView.draftingViewEnabled) return;
		var view = DraftingShaders.draftingView();
		var upscale = DraftingShaders.draftingUpscale();
		if (view == null || upscale == null) return;
		var mc = Minecraft.getInstance();
		var main = mc.getMainRenderTarget();
		var window = mc.getWindow();
		if (framebuffer == null) framebuffer = new DraftingFramebuffer(main.width, main.height);
		else framebuffer.resizeIfNeeded(main.width, main.height);
		var cfg = CCG.config.draftingView;
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
		// Pass 1: render main scene through drafting-view shader into off-screen buffer
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
		// Pass 2: copy off-screen buffer back to main render target (nearest-neighbor upscale)
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
