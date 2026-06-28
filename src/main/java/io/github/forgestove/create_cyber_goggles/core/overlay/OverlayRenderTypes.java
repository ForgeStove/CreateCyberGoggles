package io.github.forgestove.create_cyber_goggles.core.overlay;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;
/**
 * Custom {@link RenderType}s for the force overlay.
 * All use {@code POSITION_COLOR} with no depth test and translucency so arrows render on top of everything.
 */
public final class OverlayRenderTypes extends RenderType {
	private static final RenderType OVERLAY_FILL = create(
		"ccg_force_fill",
		DefaultVertexFormat.POSITION_COLOR,
		Mode.QUADS,
		1024,
		false,
		false,
		CompositeState.builder()
			.setShaderState(POSITION_COLOR_SHADER)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(NO_DEPTH_TEST)
			.setOutputState(ITEM_ENTITY_TARGET)
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.createCompositeState(false)
	);
	private static final RenderType OVERLAY_TRIANGLES = create(
		"ccg_force_triangles",
		DefaultVertexFormat.POSITION_COLOR,
		Mode.TRIANGLES,
		1024,
		false,
		false,
		CompositeState.builder()
			.setShaderState(POSITION_COLOR_SHADER)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(NO_DEPTH_TEST)
			.setOutputState(ITEM_ENTITY_TARGET)
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.createCompositeState(false)
	);
	private OverlayRenderTypes(
		String name,
		VertexFormat format,
		Mode mode,
		int bufferSize,
		boolean affectsCrumbling,
		boolean sortOnUpload,
		Runnable setup,
		Runnable cleanup
	) {
		super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, cleanup);
	}
	public static RenderType overlayFill() {
		return OVERLAY_FILL;
	}
	public static RenderType overlayTriangles() {
		return OVERLAY_TRIANGLES;
	}
}
