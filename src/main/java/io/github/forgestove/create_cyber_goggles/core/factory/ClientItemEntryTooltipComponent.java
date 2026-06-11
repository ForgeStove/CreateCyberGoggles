package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.core.util.SlotUtil;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
public final class ClientItemEntryTooltipComponent implements ClientTooltipComponent {
	private final ItemStack stack;
	private final int indent;
	private final Component label;
	public ClientItemEntryTooltipComponent(ItemStack stack, int indent, Component label) {
		this.stack = stack;
		this.indent = indent;
		this.label = label.copy().withStyle(stack.getDisplayName().getStyle());
	}
	public ItemStack stack() { return stack; }
	@Override
	public int getHeight(@NotNull Font font) {
		return SlotUtil.SIZE_SLIM;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return indentPixels(font) + SlotUtil.SIZE_SLIM + font.width(label);
	}
	@Override
	public void extractImage(@NotNull Font font, int x, int y, int width, int height, @NotNull GuiGraphicsExtractor gui) {
		var pose = gui.pose();
		pose.pushMatrix();
		var slotX = x + indentPixels(font);
		pose.translate(slotX, y);
		pose.scale(0.75F, 0.75F);
		gui.item(stack, 0, 0);
		gui.itemDecorations(font, stack, 0, 0);
		pose.popMatrix();
	}
	@Override
	public void extractText(@NotNull GuiGraphicsExtractor gui, @NotNull Font font, int x, int y) {
		var textX = x + indentPixels(font) + SlotUtil.SIZE_SLIM;
		var textY = y + Mth.floor((SlotUtil.SIZE_SLIM - font.lineHeight) / 2F);
		gui.text(font, label, textX, textY, -1, true);
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
	public record ItemEntryTooltipComponent(ItemStack stack, int indent, Component label) implements TooltipComponent {}
}
