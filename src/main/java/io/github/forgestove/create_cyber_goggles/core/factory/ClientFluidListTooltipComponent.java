package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ClientFluidListTooltipComponent implements ClientTooltipComponent {
	private final List<FluidStack> fluids;
	private final int maxColumns;
	private final int columns;
	private final int rows;
	private final int indent;
	public ClientFluidListTooltipComponent(List<FluidStack> fluids, int indent, int maxColumns) {
		this.fluids = fluids;
		this.indent = indent;
		this.maxColumns = maxColumns;
		columns = Math.min(fluids.size(), maxColumns);
		rows = (fluids.size() + maxColumns - 1) / maxColumns;
	}
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(
			FluidListTooltipComponent.class,
			data -> new ClientFluidListTooltipComponent(data.fluids(), data.indent(), data.maxColumns())
		);
	}
	@Override
	public int getHeight() {
		return Math.max(1, rows) * SlotUtil.SIZE + 4;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return columns * SlotUtil.SIZE + indentPixels(font);
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
		for (var i = 0; i < fluids.size(); i++) {
			var stack = fluids.get(i);
			if (stack.isEmpty()) return;
			var col = i % maxColumns;
			var row = i / maxColumns;
			var slotX = x + col * SlotUtil.SIZE + indentPixels(font);
			var slotY = y + row * SlotUtil.SIZE;
			renderFluidBar(gui, stack, slotX, slotY, SlotUtil.SIZE, SlotUtil.SIZE);
			renderFluidCount(gui, font, stack, slotX, slotY);
		}
	}
	public static void renderFluidBar(@NotNull GuiGraphics gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		gui.blitSprite(SlotUtil.SLOT, x, y, 0, SlotUtil.SIZE, SlotUtil.SIZE + 2);
		var innerX = x + 1;
		var innerY = y + 1;
		var innerW = Math.max(0, width - 2);
		var innerH = Math.max(0, height - 2);
		renderFluid(gui, stack, innerX, innerY, innerW, innerH);
	}
	private static void renderFluidCount(@NotNull GuiGraphics gui, @NotNull Font font, @NotNull FluidStack stack, int x, int y) {
		var text = AmountUtil.formatFluidAmount(stack.getAmount());
		gui.drawString(font, text, x + SlotUtil.SIZE - font.width(text), y + SlotUtil.SIZE - font.lineHeight, 0xFFFFFF, true);
	}
	private static void renderFluid(@NotNull GuiGraphics gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		var ext = IClientFluidTypeExtensions.of(stack.getFluid());
		var still = ext.getStillTexture(stack);
		var sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
		var tint = ext.getTintColor(stack);
		var r = ARGB32.red(tint) / 255F;
		var g = ARGB32.green(tint) / 255F;
		var b = ARGB32.blue(tint) / 255F;
		var a = ARGB32.alpha(tint) / 255F;
		for (var dx = 0; dx < width; dx += 16) {
			var sliceWidth = Math.min(16, width - dx);
			gui.blit(x + dx, y, 0, sliceWidth, height, sprite, r, g, b, a);
		}
	}
	public record FluidListTooltipComponent(List<FluidStack> fluids, int indent, int maxColumns) implements TooltipComponent {}
}
