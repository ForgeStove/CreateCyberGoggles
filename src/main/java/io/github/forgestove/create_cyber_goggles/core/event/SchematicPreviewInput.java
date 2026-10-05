package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.schematic.SchematicPreviewAccess;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseScrolled.Pre;
/**
 * 拦截落在蓝图桌 3D 预览面板范围内的滚轮事件，用于缩放。
 */
public final class SchematicPreviewInput {
	public static void onMouseScrolled(Pre event) {
		if (!CCG.config.schematic.preview.previewEnabled) return;
		if (!(event.getScreen() instanceof SchematicPreviewAccess access)) return;
		var panel = access.ccg$getPreviewPanel();
		if (panel == null || !panel.isMouseOver(event.getMouseX(), event.getMouseY())) return;
		panel.onScroll(event.getScrollDeltaY());
		event.setCanceled(true);
	}
}
