package io.github.forgestove.create_cyber_goggles.core.overlay.drafting;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
/**
 * Thin wrapper around a {@link TextureTarget} used as an intermediate render target
 * for the drafting-view post-processing shader.
 */
public final class DraftingFramebuffer {
	final TextureTarget target;
	DraftingFramebuffer(final int width, final int height) {
		target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
		target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	}
	void resizeIfNeeded(final int width, final int height) {
		if (target.width == width && target.height == height) return;
		target.resize(width, height, Minecraft.ON_OSX);
	}
}
