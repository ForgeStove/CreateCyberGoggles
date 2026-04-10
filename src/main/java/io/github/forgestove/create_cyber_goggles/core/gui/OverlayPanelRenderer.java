package io.github.forgestove.create_cyber_goggles.core.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.create_cyber_goggles.core.util.SlotUtil;
import net.minecraft.client.gui.GuiGraphics;
public final class OverlayPanelRenderer {
	private static final int BG = 0xFFC6C6C6;
	private static final int LIGHT = 0xFFFFFFFF;
	private static final int DARK = 0xFF555555;
	private static final int DARKER = 0xFF373737;
	public static void renderPanel(GuiGraphics graphics, int width, int height, float r, float g, float b) {
		RenderSystem.setShaderColor(r, g, b, 1F);
		graphics.fill(0, 0, width, height, BG);
		graphics.fill(0, 0, width, 2, LIGHT);
		graphics.fill(0, 0, 2, height, LIGHT);
		graphics.fill(0, height - 2, width, height, DARK);
		graphics.fill(width - 2, 0, width, height, DARK);
		graphics.fill(1, height - 1, width - 1, height, DARKER);
		graphics.fill(width - 1, 1, width, height - 1, DARKER);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	public static void renderTintedSlot(GuiGraphics graphics, int x, int y, float r, float g, float b) {
		RenderSystem.setShaderColor(r, g, b, 1F);
		graphics.blitSprite(SlotUtil.SLOT, x, y, 0, SlotUtil.SIZE, SlotUtil.SIZE);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
}
