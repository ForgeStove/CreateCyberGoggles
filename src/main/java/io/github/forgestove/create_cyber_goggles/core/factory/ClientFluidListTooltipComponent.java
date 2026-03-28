package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class ClientFluidListTooltipComponent implements ClientTooltipComponent {
	private static final int SLOT_WIDTH = 18;
	private static final int SLOT_HEIGHT = 20;
	private final List<FluidStack> fluids;
	private final int maxColumns;
	private final int columns;
	private final int rows;
	private final int indent;
	public ClientFluidListTooltipComponent(List<FluidStack> fluids, int indent, int maxColumns) {
		this.fluids = fluids;
		this.indent = indent;
		this.maxColumns = maxColumns;
		this.columns = Math.min(fluids.size(), maxColumns);
		this.rows = (fluids.size() + maxColumns - 1) / maxColumns;
	}
	public static void register() {
		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof FluidListTooltipComponent fluidList)
				return new ClientFluidListTooltipComponent(fluidList.fluids(), fluidList.indent(), fluidList.maxColumns());
			return null;
		});
	}
	public static void renderFluidBar(@NotNull GuiGraphics guiGraphics, FluidStack stack, int x, int y, int width, int height) {
		if (stack.isEmpty()) return;
		var innerX = x + 1;
		var innerY = y + 1;
		var innerW = Math.max(0, width - 2);
		var innerH = Math.max(0, height - 2);
		ClientFluidEntryTooltipComponent.renderFluid(guiGraphics, stack, innerX, innerY, innerW, innerH);
	}
	@Override
	public int getHeight() {
		return SLOT_HEIGHT + Math.max(0, rows - 1) * (SLOT_HEIGHT - 2) + 2;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return columns * SLOT_WIDTH + indentPixels(font);
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
		for (var i = 0; i < fluids.size(); i++) {
			var col = i % maxColumns;
			var row = i / maxColumns;
			var slotX = x + col * SLOT_WIDTH + indentPixels(font);
			var slotY = y + row * (SLOT_HEIGHT - 2);
			renderFluidBar(guiGraphics, fluids.get(i), slotX, slotY, SLOT_WIDTH, SLOT_HEIGHT - 2);
		}
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
	public record FluidListTooltipComponent(List<FluidStack> fluids, int indent, int maxColumns) implements TooltipComponent {}
}
