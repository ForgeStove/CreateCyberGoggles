package io.github.forgestove.create_cyber_goggles.core.factory;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.create_cyber_goggles.core.util.SlotUtil;
import net.minecraft.client.Minecraft;
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
public class ClientFluidListTooltipComponent implements ClientTooltipComponent {
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
	public static void renderFluidBar(@NotNull GuiGraphics gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		gui.blitSprite(SlotUtil.SLOT, x, y, 0, SlotUtil.SIZE, SlotUtil.SIZE + 2);
		if (stack.isEmpty()) return;
		var innerX = x + 1;
		var innerY = y + 1;
		var innerW = Math.max(0, width - 2);
		var innerH = Math.max(0, height - 2);
		renderFluid(gui, stack, innerX, innerY, innerW, innerH);
	}
	private static void renderFluid(@NotNull GuiGraphics gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		if (stack.isEmpty()) return;
		var ext = IClientFluidTypeExtensions.of(stack.getFluid());
		var still = ext.getStillTexture(stack);
		var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
		var tint = ext.getTintColor(stack);
		var r = ARGB32.red(tint) / 255F;
		var g = ARGB32.green(tint) / 255F;
		var b = ARGB32.blue(tint) / 255F;
		var a = ARGB32.alpha(tint) / 255F;
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(r, g, b, a);
		for (var dx = 0; dx < width; dx += 16) {
			var sliceWidth = Math.min(16, width - dx);
			if (sliceWidth <= 0) break;
			gui.blit(x + dx, y, 0, sliceWidth, height, sprite);
		}
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	@Override
	public int getHeight() {
		return Math.max(1, rows) * SlotUtil.SIZE + 4;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return columns * SlotUtil.SIZE + indentPixels(font);
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
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
