package io.github.forgestove.create_cyber_goggles.core.factory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import io.github.forgestove.create_cyber_goggles.core.util.SlotUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
	public static void renderFluidBar(@NotNull GuiGraphics gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		gui.blitSprite(RenderPipelines.GUI_TEXTURED, SlotUtil.SLOT, x, y, SlotUtil.SIZE, SlotUtil.SIZE + 2);
		if (stack.isEmpty()) return;
		var innerX = x + 1;
		var innerY = y + 1;
		var innerW = Math.max(0, width - 2);
		var innerH = Math.max(0, height - 2);
		renderFluid(gui, stack, innerX, innerY, innerW, innerH);
	}
	private static void renderFluid(@NotNull GuiGraphics gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		if (stack.isEmpty()) return;
		var fluid = stack.getFluid();
		var fluidKey = BuiltInRegistries.FLUID.getKey(fluid);
		var stillLocation = Identifier.fromNamespaceAndPath(fluidKey.getNamespace(), "block/" + fluidKey.getPath() + "_still");
		var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(stillLocation);
		var tint = getFluidTint(fluid);
		for (var dx = 0; dx < width; dx += 16) {
			var sliceWidth = Math.min(16, width - dx);
			if (sliceWidth <= 0) break;
			gui.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + dx, y, sliceWidth, height, tint);
		}
	}
	private static int getFluidTint(Fluid fluid) {
		var defaultState = fluid.defaultFluidState();
		var mapColor = defaultState.createLegacyBlock().getBlock().defaultMapColor();
		return mapColor.col | 0xFF000000;
	}
	@Override
	public int getHeight(@NotNull Font font) {
		return Math.max(1, rows) * SlotUtil.SIZE + 4;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return columns * SlotUtil.SIZE + indentPixels(font);
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, int width, int height, @NotNull GuiGraphics gui) {
		for (var i = 0; i < fluids.size(); i++) {
			var col = i % maxColumns;
			var row = i / maxColumns;
			var slotX = x + col * SlotUtil.SIZE + indentPixels(font);
			var slotY = y + row * SlotUtil.SIZE;
			renderFluidBar(gui, fluids.get(i), slotX, slotY, SlotUtil.SIZE, SlotUtil.SIZE);
		}
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
	public record FluidListTooltipComponent(List<FluidStack> fluids, int indent, int maxColumns) implements TooltipComponent {}
}
