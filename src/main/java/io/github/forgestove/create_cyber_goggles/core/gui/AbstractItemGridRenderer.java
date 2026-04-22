package io.github.forgestove.create_cyber_goggles.core.gui;
import io.github.forgestove.create_cyber_goggles.core.api.TooltipOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
public abstract class AbstractItemGridRenderer implements TooltipOverlayRenderer {
	public abstract boolean supports(ItemStack stack);
	public abstract @Nullable OverlayData buildItemGrid(ItemStack stack);
	@Override
	public int width(ItemStack stack) {
		if (!supports(stack)) return 0;
		var data = buildItemGrid(stack);
		if (data == null || data.items() == null) return 0;
		return ItemGridRenderUtil.resolveColumns(data) * SlotUtil.SIZE + ItemGridRenderUtil.PAD * 2;
	}
	@Override
	public int height(ItemStack stack) {
		if (!supports(stack)) return 0;
		var data = buildItemGrid(stack);
		if (data == null || data.items() == null) return 0;
		var columns = ItemGridRenderUtil.resolveColumns(data);
		var rows = Math.max(1, Mth.ceil((float) data.items().size() / columns));
		return rows * SlotUtil.SIZE + ItemGridRenderUtil.PAD * 2;
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
		var cols = ItemGridRenderUtil.resolveColumns(data);
		ItemGridRenderUtil.renderItemGrid(gui, data.items, cols, x, y, r, g, b, data.zeroCountSlots);
	}
	public record OverlayData(List<ItemStack> items, int columns, Set<Integer> zeroCountSlots) {
		public OverlayData(List<ItemStack> items, int columns) {
			this(items, columns, Set.of());
		}
	}
}
