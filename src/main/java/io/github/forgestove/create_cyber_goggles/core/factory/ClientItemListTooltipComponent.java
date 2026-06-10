package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.core.gui.AbstractItemGridRenderer;
import io.github.forgestove.create_cyber_goggles.core.util.SlotUtil;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class ClientItemListTooltipComponent implements ClientTooltipComponent {
	private final List<ItemStack> items;
	private final int maxColumns;
	private final int columns;
	private final int rows;
	private final int indent;
	public ClientItemListTooltipComponent(List<ItemStack> items, int indent, int maxColumns) {
		this.items = items;
		this.indent = indent;
		this.maxColumns = maxColumns;
		columns = Math.min(items.size(), maxColumns);
		rows = (items.size() + maxColumns - 1) / maxColumns;
	}
	@Override
	public int getHeight(@NotNull Font font) {
		return Math.max(1, rows) * SlotUtil.SIZE + 2;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return columns * SlotUtil.SIZE + indentPixels(font);
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, int width, int height, @NotNull GuiGraphics gui) {
		for (var i = 0; i < items.size(); i++) {
			var col = i % maxColumns;
			var row = i / maxColumns;
			var slotX = x + col * SlotUtil.SIZE + indentPixels(font);
			var slotY = y + row * SlotUtil.SIZE;
			renderSlot(gui, font, items.get(i), slotX, slotY);
		}
	}
	private void renderSlot(GuiGraphics gui, Font font, ItemStack stack, int x, int y) {
		gui.blitSprite(RenderPipelines.GUI_TEXTURED, SlotUtil.SLOT, x, y, SlotUtil.SIZE, SlotUtil.SIZE);
		gui.renderItem(stack, x + 1, y + 1);
		if (AbstractItemGridRenderer.isCFLCompressedTank(stack) && stack.getCount() > 1)
			gui.renderItemDecorations(font, stack, x + 1, y + 1, AbstractItemGridRenderer.formatFluidAmount(stack.getCount()));
		else gui.renderItemDecorations(font, stack, x + 1, y + 1);
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
	public record ItemListTooltipComponent(List<ItemStack> items, int indent, int maxColumns) implements TooltipComponent {}
}
