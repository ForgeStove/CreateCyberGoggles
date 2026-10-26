package io.github.forgestove.create_cyber_goggles.schematic;
import com.mojang.blaze3d.platform.Window;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.IntSupplier;
/**
 * 蓝图桌旁的 3D 预览面板：摆位、跟随当前选中的蓝图并处理鼠标交互。
 */
public class SchematicTablePreviewController {
	private static final int PANEL_GAP = 4;
	private static final int SCREEN_MARGIN = 6;
	private static final int MIN_PANEL_SIZE = 60;
	private final SchematicPreviewPanel panel = new SchematicPreviewPanel();
	private final IntSupplier schematicIndex;
	@Nullable private Rect2i area;
	public SchematicTablePreviewController(IntSupplier schematicIndex) {
		this.schematicIndex = schematicIndex;
	}
	public SchematicPreviewPanel panel() {
		return panel;
	}
	@Nullable
	public Rect2i area() {
		return area;
	}
	/** 优先贴左侧与界面的空隙，其次贴上方的空隙；两处均放不下则不显示 */
	public void updateArea(int guiLeft, int guiTop, int backgroundWidth, List<Rect2i> extraAreas) {
		if (!CCG.config.schematic.preview.previewEnabled) {
			area = null;
			return;
		}
		Window window = Minecraft.getInstance().getWindow();
		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();
		int panelWidth = Math.clamp(screenWidth - SCREEN_MARGIN * 2, 1, CCG.config.schematic.preview.sidePanelWidth);
		int panelHeight = Math.clamp(screenHeight - SCREEN_MARGIN * 2, 1, CCG.config.schematic.preview.maxHeight);
		int minPanelHeight = Math.min(MIN_PANEL_SIZE, panelHeight);
		int occupiedLeft = guiLeft;
		int occupiedTop = guiTop;
		int occupiedRight = guiLeft + backgroundWidth;
		for (Rect2i extra : extraAreas) {
			occupiedLeft = Math.min(occupiedLeft, extra.getX());
			occupiedTop = Math.min(occupiedTop, extra.getY());
			occupiedRight = Math.max(occupiedRight, extra.getX() + extra.getWidth());
		}
		int leftRoom = Math.max(0, occupiedLeft - PANEL_GAP - SCREEN_MARGIN);
		int aboveRoom = Math.max(0, occupiedTop - PANEL_GAP - SCREEN_MARGIN);
		int x, y;
		// 左侧放得下完整面板，或上方连最小高度都没有 → 贴左（宽度不足时按空隙收窄）
		if (leftRoom >= panelWidth || aboveRoom < minPanelHeight) {
			panelWidth = Math.min(panelWidth, leftRoom);
			x = occupiedLeft - PANEL_GAP - panelWidth;
			y = clampToMargin(guiTop, screenHeight - SCREEN_MARGIN - panelHeight);
		} else {
			panelWidth = 204;
			occupiedLeft -= 54;
			panelHeight = Math.min(panelHeight, aboveRoom);
			x = clampToMargin((occupiedLeft + occupiedRight - panelWidth) / 2, screenWidth - SCREEN_MARGIN - panelWidth);
			y = occupiedTop - PANEL_GAP - panelHeight;
		}
		area = new Rect2i(x, y, panelWidth, panelHeight);
	}
	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		if (area == null) return;
		SchematicImageUtil.getSchematicNameFromIndex(schematicIndex.getAsInt()).ifPresent(panel::setSelected);
		Minecraft mc = Minecraft.getInstance();
		boolean leftDown = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		panel.updateMouse(mouseX, mouseY, leftDown);
		panel.render(graphics, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}
	private static int clampToMargin(int value, int max) {
		return max < SCREEN_MARGIN ? SCREEN_MARGIN : Mth.clamp(value, SCREEN_MARGIN, max);
	}
}
