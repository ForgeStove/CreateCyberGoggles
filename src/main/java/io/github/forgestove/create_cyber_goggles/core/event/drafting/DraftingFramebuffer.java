package io.github.forgestove.create_cyber_goggles.core.event.drafting;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
/**
 * 对 {@link TextureTarget} 的轻量封装，用作绘图视图（drafting-view）后期处理着色器的中间渲染目标。
 */
public class DraftingFramebuffer {
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
