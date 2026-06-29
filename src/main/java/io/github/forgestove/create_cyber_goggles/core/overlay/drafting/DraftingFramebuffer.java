package io.github.forgestove.create_cyber_goggles.core.overlay.drafting;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
/**
 * Thin wrapper around a {@link TextureTarget} used as an intermediate render target
 * for the drafting-view post-processing shader.
 */
public final class DraftingFramebuffer {
	public final TextureTarget target;
	public DraftingFramebuffer(int width, int height) {
		target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
		target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	}
	public void resizeIfNeeded(int width, int height) {
		if (target.width == width && target.height == height) return;
		target.resize(width, height, Minecraft.ON_OSX);
	}
}
