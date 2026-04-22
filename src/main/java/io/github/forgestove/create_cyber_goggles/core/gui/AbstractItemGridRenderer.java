package io.github.forgestove.create_cyber_goggles.core.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.create_cyber_goggles.core.api.TooltipOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public abstract class AbstractItemGridRenderer implements TooltipOverlayRenderer {
	public static final int PAD = 4;
	private static final int BG = 0xFFC6C6C6;
	private static final int LIGHT = 0xFFFFFFFF;
	private static final int DARK = 0xFF555555;
	private static final int DARKER = 0xFF373737;
	public static int resolveColumns(OverlayData data) {
		return Mth.clamp(data.columns(), 1, Math.max(1, data.items().size()));
	}
	public static void renderItemGrid(
		GuiGraphics gui,
		@NotNull List<ItemStack> items,
		int columns,
		int x,
		int y,
		float r,
		float g,
		float b,
		Set<Integer> zeroCountSlots
	) {
		var rows = Math.max(1, Mth.ceil((float) items.size() / columns));
		var panelWidth = columns * SlotUtil.SIZE + PAD * 2;
		var panelHeight = rows * SlotUtil.SIZE + PAD * 2;
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(x, y, 600F);
		renderPanel(gui, panelWidth, panelHeight, r, g, b);
		for (var i = 0; i < items.size(); i++) {
			var col = i % columns;
			var row = i / columns;
			var slotX = PAD + col * SlotUtil.SIZE;
			var slotY = PAD + row * SlotUtil.SIZE;
			renderTintedSlot(gui, slotX, slotY, r, g, b);
			var item = items.get(i);
			gui.renderItem(item, slotX + 1, slotY + 1);
			if (zeroCountSlots.contains(i)) gui.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1, "0");
			else gui.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1);
		}
		pose.popPose();
	}
	public static void renderPanel(GuiGraphics gui, int width, int height, float r, float g, float b) {
		RenderSystem.setShaderColor(r, g, b, 1F);
		gui.fill(0, 0, width, height, BG);
		gui.fill(0, 0, width, 2, LIGHT);
		gui.fill(0, 0, 2, height, LIGHT);
		gui.fill(0, height - 2, width, height, DARK);
		gui.fill(width - 2, 0, width, height, DARK);
		gui.fill(1, height - 1, width - 1, height, DARKER);
		gui.fill(width - 1, 1, width, height - 1, DARKER);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	public static void renderTintedSlot(GuiGraphics gui, int x, int y, float r, float g, float b) {
		RenderSystem.setShaderColor(r, g, b, 1F);
		gui.blitSprite(SlotUtil.SLOT, x, y, 0, SlotUtil.SIZE, SlotUtil.SIZE);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	public abstract boolean supports(ItemStack stack);
	public abstract @Nullable OverlayData buildItemGrid(ItemStack stack);
	@Override
	public int width(ItemStack stack) {
		if (!supports(stack)) return 0;
		var data = buildItemGrid(stack);
		if (data == null || data.items() == null) return 0;
		return resolveColumns(data) * SlotUtil.SIZE + PAD * 2;
	}
	@Override
	public int height(ItemStack stack) {
		if (!supports(stack)) return 0;
		var data = buildItemGrid(stack);
		if (data == null || data.items() == null) return 0;
		var columns = resolveColumns(data);
		var rows = Math.max(1, Mth.ceil((float) data.items().size() / columns));
		return rows * SlotUtil.SIZE + PAD * 2;
	}
	@Override
	public void render(GuiGraphics gui, ItemStack stack, int x, int y) {
		var data = buildItemGrid(stack);
		if (data == null) return;
		var color = NativeImageUtil.getColor(stack);
		var r = color.getRed() / 255F;
		var g = color.getGreen() / 255F;
		var b = color.getBlue() / 255F;
		if (data.items == null) return;
		var cols = resolveColumns(data);
		renderItemGrid(gui, data.items, cols, x, y, r, g, b, data.zeroCountSlots);
	}
	public record OverlayData(List<ItemStack> items, int columns, Set<Integer> zeroCountSlots) {
		public OverlayData(List<ItemStack> items, int columns) {
			this(items, columns, Set.of());
		}
	}
}
