package io.github.forgestove.create_cyber_goggles.core.gui;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ContainerRenderer implements TooltipOverlayRenderer {
	private static final int COLUMNS = 9;
	private static final int PADDING = 4;
	private static boolean isVanilla27Container(ItemStack stack) {
		var item = stack.getItem();
		if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) return true;
		return item == Items.CHEST || item == Items.TRAPPED_CHEST || item == Items.BARREL || item == Items.CHEST_MINECART;
	}
	private static int resolveSlots(ItemStack stack, int storedSlots) {
		return isVanilla27Container(stack) ? 27 : storedSlots;
	}
	private static Grid resolveGrid(int slots) {
		var columns = Mth.clamp(slots, 1, COLUMNS);
		var rows = Math.max(1, Mth.ceil((float) slots / COLUMNS));
		return new Grid(columns, rows);
	}
	@Override
	public boolean supports(ItemStack stack) {
		if (!CCG.config.tooltip.container) return false;
		var container = stack.getComponents().get(DataComponents.CONTAINER);
		if (container == null) return false;
		for (var i = 0; i < container.getSlots(); i++)
			if (!container.getStackInSlot(i).isEmpty()) return true;
		return false;
	}
	@Override
	public int width(ItemStack stack) {
		var container = stack.getComponents().get(DataComponents.CONTAINER);
		if (container == null) return 0;
		return resolveGrid(resolveSlots(stack, container.getSlots())).columns() * SlotUtil.SIZE + PADDING * 2;
	}
	@Override
	public int height(ItemStack stack) {
		var container = stack.getComponents().get(DataComponents.CONTAINER);
		if (container == null) return 0;
		return resolveGrid(resolveSlots(stack, container.getSlots())).rows() * SlotUtil.SIZE + PADDING * 2;
	}
	@Override
	public void render(GuiGraphics graphics, ItemStack stack, int x, int y) {
		var container = stack.getComponents().get(DataComponents.CONTAINER);
		if (container == null) return;
		var font = mc.font;
		var storedSlots = container.getSlots();
		if (storedSlots <= 0) return;
		var slots = resolveSlots(stack, storedSlots);
		var grid = resolveGrid(slots);
		var columns = grid.columns();
		var rows = grid.rows();
		var gridWidth = columns * SlotUtil.SIZE;
		var gridHeight = rows * SlotUtil.SIZE;
		var panelWidth = gridWidth + PADDING * 2;
		var panelHeight = gridHeight + PADDING * 2;
		var color = NativeImageUtil.getColor(stack);
		var r = color.getRed() / 255F;
		var g = color.getGreen() / 255F;
		var b = color.getBlue() / 255F;
		var pose = graphics.pose();
		pose.pushPose();
		pose.translate(x, y, 600F);
		OverlayPanelRenderer.renderPanel(graphics, panelWidth, panelHeight, r, g, b);
		for (var i = 0; i < slots; i++) {
			var col = i % columns;
			var row = i / columns;
			var slotX = PADDING + col * SlotUtil.SIZE;
			var slotY = PADDING + row * SlotUtil.SIZE;
			var item = i < storedSlots ? container.getStackInSlot(i) : ItemStack.EMPTY;
			graphics.renderItem(item, slotX + 1, slotY + 1);
			graphics.renderItemDecorations(font, item, slotX + 1, slotY + 1);
			OverlayPanelRenderer.renderTintedSlot(graphics, slotX, slotY, r, g, b);
		}
		pose.popPose();
	}
	public record Grid(int columns, int rows) {}
}
