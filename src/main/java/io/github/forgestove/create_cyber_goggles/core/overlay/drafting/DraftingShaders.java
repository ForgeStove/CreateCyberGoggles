package io.github.forgestove.create_cyber_goggles.core.overlay.drafting;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
/**
 * Holds the two shader instances used by the drafting-view effect.
 * Registered via {@link RegisterShadersEvent}.
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
	// ownership transferred to event registration
	public static void onRegisterShaders(final RegisterShadersEvent event) {
		try {
			event.registerShader(
				new ShaderInstance(
					event.getResourceProvider(),
					ResourceLocation.fromNamespaceAndPath("create_cyber_goggles", "drafting_view"),
					DefaultVertexFormat.POSITION
				), loaded -> draftingView = loaded
			);
			event.registerShader(
				new ShaderInstance(
					event.getResourceProvider(),
					ResourceLocation.fromNamespaceAndPath("create_cyber_goggles", "drafting_upscale"),
					DefaultVertexFormat.POSITION
				), loaded -> draftingUpscale = loaded
			);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to register CreateCyberGoggles drafting-view shaders", e);
		}
	}
}
