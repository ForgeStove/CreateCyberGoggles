package io.github.forgestove.create_cyber_goggles.core.event.drafting;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getCCGRes;
/**
 * 持有绘图视图效果所使用的两个着色器实例。
 * 通过 {@link RegisterShadersEvent} 注册。
 */
public final class DraftingShaders {
	private static @Nullable ShaderInstance draftingView;
	private static @Nullable ShaderInstance draftingUpscale;
	@Nullable
	public static ShaderInstance draftingView() {
		return draftingView;
	}
	@Nullable
	public static ShaderInstance draftingUpscale() {
		return draftingUpscale;
	}
	public static void register(RegisterShadersEvent event) {
		try {
			event.registerShader(
				new ShaderInstance(event.getResourceProvider(), getCCGRes("drafting_view"), DefaultVertexFormat.POSITION),
				loaded -> draftingView = loaded
			);
			event.registerShader(
				new ShaderInstance(event.getResourceProvider(), getCCGRes("drafting_upscale"), DefaultVertexFormat.POSITION),
				loaded -> draftingUpscale = loaded
			);
		} catch (IOException e) {
			throw new RuntimeException("Failed to register CreateCyberGoggles drafting-view shaders", e);
		}
	}
}
