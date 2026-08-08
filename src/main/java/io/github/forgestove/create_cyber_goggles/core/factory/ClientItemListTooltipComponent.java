package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.compat.fluidlogistics.PackageTankHelper;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class ClientItemListTooltipComponent implements ClientTooltipComponent {
	private final ItemListTooltipComponent c;
	private final int columns;
	private final int rows;
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(ItemListTooltipComponent.class, ClientItemListTooltipComponent::new);
	}
	public ClientItemListTooltipComponent(ItemListTooltipComponent c) {
		this.c = c;
		columns = Math.min(c.items.size(), c.maxColumns);
		rows = (c.items.size() + c.maxColumns - 1) / c.maxColumns;
	}
	@Override
	public int getHeight() {
		return Math.max(1, rows) * SlotUtil.SIZE + 2;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return columns * SlotUtil.SIZE + indentPixels(font);
	}
	private int indentPixels(@NotNull Font font) {
		return c.indent * font.width(" ");
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
		for (var i = 0; i < c.items.size(); i++) {
			var col = i % c.maxColumns;
			var row = i / c.maxColumns;
			var slotX = x + col * SlotUtil.SIZE + indentPixels(font);
			var slotY = y + row * SlotUtil.SIZE;
			renderSlot(gui, font, c.items.get(i), slotX, slotY);
		}
	}
	private void renderSlot(GuiGraphics gui, Font font, ItemStack stack, int x, int y) {
		gui.blitSprite(SlotUtil.SLOT, x, y, 0, SlotUtil.SIZE, SlotUtil.SIZE);
		gui.renderItem(stack, x + 1, y + 1);
		var count = stack.getCount();
		if (PackageTankHelper.isCFLCompressedTank(stack))
			gui.renderItemDecorations(font, stack, x + 1, y + 1, AmountUtil.formatFluidAmount(count));
		else if (count > 1) gui.renderItemDecorations(font, stack, x + 1, y + 1, AmountUtil.formatItemCount(count));
	}
	public record ItemListTooltipComponent(List<ItemStack> items, int indent, int maxColumns) implements TooltipComponent {}
}
