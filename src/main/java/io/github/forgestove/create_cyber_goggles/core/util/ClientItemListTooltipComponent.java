package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class ClientItemListTooltipComponent implements ClientTooltipComponent {
	private static final ResourceLocation SLOT_SPRITE = new ResourceLocation("textures/gui/container/bundle.png");
	private static final int SLOT_WIDTH = 18;
	private static final int SLOT_HEIGHT = 18;
	private final List<ItemStack> items;
	private final int maxColumns;
	private final int columns;
	private final int rows;
	private final int indent;
	public ClientItemListTooltipComponent(List<ItemStack> items, int maxColumns, int indent) {
		this.items = items;
		this.maxColumns = maxColumns;
		this.columns = Math.min(items.size(), maxColumns);
		this.rows = (items.size() + maxColumns - 1) / maxColumns;
		this.indent = Math.max(0, indent);
	}
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(
			ItemListTooltipComponent.class,
			data -> new ClientItemListTooltipComponent(data.items(), data.maxColumns(), data.indent())
		);
	}
	@Override
	public int getHeight() {
		return SLOT_HEIGHT + Math.max(0, rows - 1) * SLOT_HEIGHT + 4;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return columns * SLOT_WIDTH + indentPixels(font);
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
		for (var i = 0; i < items.size(); i++) {
			var col = i % maxColumns;
			var row = i / maxColumns;
			var slotX = x + col * SLOT_WIDTH + indentPixels(font);
			var slotY = y + row * SLOT_HEIGHT;
			renderSlot(guiGraphics, font, items.get(i), slotX, slotY);
		}
	}
	private void renderSlot(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y) {
		guiGraphics.blit(SLOT_SPRITE, x, y, 0, 0, SLOT_WIDTH, SLOT_HEIGHT, 128, 128);
		guiGraphics.renderItem(stack, x + 1, y + 1);
		guiGraphics.renderItemDecorations(font, stack, x + 1, y + 1);
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
}





