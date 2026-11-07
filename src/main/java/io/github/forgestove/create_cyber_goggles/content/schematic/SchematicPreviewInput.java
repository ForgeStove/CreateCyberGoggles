package io.github.forgestove.create_cyber_goggles.content.schematic;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseScrolled.Pre;
public final class SchematicPreviewInput {
	public static void onMouseScrolled(Pre event) {
		if (!CCG.config.schematic.preview.previewEnabled) return;
		if (!(event.getScreen() instanceof SchematicPreviewAccess access)) return;
		SchematicPreviewPanel panel = access.ccg$getPreviewPanel();
		if (panel == null || !panel.isMouseOver(event.getMouseX(), event.getMouseY())) return;
		panel.onScroll(event.getScrollDeltaY());
		event.setCanceled(true);
	}
}
