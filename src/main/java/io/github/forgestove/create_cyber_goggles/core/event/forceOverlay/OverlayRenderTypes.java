package io.github.forgestove.create_cyber_goggles.core.event.forceOverlay;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;
/**
 * 力覆盖层的自定义 {@link RenderType}。
 * 全部使用 {@link DefaultVertexFormat#POSITION_COLOR}，不进行深度测试并启用半透明，使箭头渲染在所有内容之上。
 */
public class OverlayRenderTypes extends RenderType {
	public static final RenderType OVERLAY_FILL = create(
		"ccg_force_fill",
		DefaultVertexFormat.POSITION_COLOR,
		Mode.QUADS,
		1024,
		false,
		false,
		getState()
	), OVERLAY_TRIANGLES = create(
		"ccg_force_triangles",
		DefaultVertexFormat.POSITION_COLOR,
		Mode.TRIANGLES,
		1024,
		false,
		false,
		getState()
	);
	public OverlayRenderTypes(
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
	private static @NotNull CompositeState getState() {
		return CompositeState.builder()
			.setShaderState(POSITION_COLOR_SHADER)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(NO_DEPTH_TEST)
			.setOutputState(ITEM_ENTITY_TARGET)
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.createCompositeState(false);
	}
}
