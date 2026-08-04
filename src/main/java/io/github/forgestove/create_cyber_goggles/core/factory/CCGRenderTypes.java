package io.github.forgestove.create_cyber_goggles.core.factory;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
public class CCGRenderTypes extends RenderType {
	// 力覆盖层：POSITION_COLOR、不深度测试、半透明，使箭头渲染在所有内容之上
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
	// 剪贴板图标：text shader 匹配 BLOCK 格式且 light 有效，NO_DEPTH_TEST 无视背景深度，始终显示在剪贴板上方
	public static final RenderType ICON_NO_DEPTH = create(
		"ccg_clipboard_icon",
		DefaultVertexFormat.BLOCK,
		Mode.QUADS,
		2097152,
		true,
		true,
		CompositeState.builder()
			.setShaderState(RENDERTYPE_TEXT_SHADER)
			.setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setLightmapState(LIGHTMAP)
			.setDepthTestState(NO_DEPTH_TEST)
			.setWriteMaskState(COLOR_WRITE)
			.createCompositeState(true)
	);
	public CCGRenderTypes(
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
	// 剪贴板背景/贴图：正常深度遮挡并写深度（挡住云等透明物）
	public static @NotNull RenderType textWithDepth(ResourceLocation location, boolean polygonOffset) {
		var builder = CompositeState.builder()
			.setShaderState(RENDERTYPE_TEXT_SHADER)
			.setTextureState(new TextureStateShard(location, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setLightmapState(LIGHTMAP)
			.setDepthTestState(LEQUAL_DEPTH_TEST)
			.setWriteMaskState(COLOR_DEPTH_WRITE);
		if (polygonOffset) builder.setLayeringState(POLYGON_OFFSET_LAYERING);
		return create(
			"ccg_clipboard_text",
			DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
			Mode.QUADS,
			786432,
			false,
			true,
			builder.createCompositeState(false)
		);
	}
}
