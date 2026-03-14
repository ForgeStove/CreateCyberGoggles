package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
public class ClientItemEntryTooltipComponent implements ClientTooltipComponent {
	private static final int SLOT_WIDTH = 18;
	private static final int SLOT_HEIGHT = 20;
	private static final int TEXT_GAP = 2;
	private final ItemStack stack;
	private final Component label;
	private final int indent;
	public ClientItemEntryTooltipComponent(ItemStack stack, Component label, int indent) {
		this.stack = stack;
		this.label = label;
		this.indent = Math.max(0, indent);
	}
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(
			ItemEntryTooltipComponent.class,
			data -> new ClientItemEntryTooltipComponent(data.stack(), data.label(), data.indent())
		);
	}
	@Override
	public int getHeight() {
		return SLOT_HEIGHT;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return indentPixels(font) + SLOT_WIDTH + TEXT_GAP + font.width(label);
	}
	@Override
	public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, @NotNull MultiBufferSource.BufferSource source) {
		var textX = x + indentPixels(font) + SLOT_WIDTH + TEXT_GAP;
		var textY = y + Mth.floor((SLOT_HEIGHT - font.lineHeight) / 2F) + 1;
		font.drawInBatch(label, textX, textY, -1, true, matrix, source, Font.DisplayMode.NORMAL, 0, 15728880);
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
		var slotX = x + indentPixels(font);
		guiGraphics.renderItem(stack, slotX + 1, y + 1);
		guiGraphics.renderItemDecorations(font, stack, slotX + 1, y + 1);
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
}

