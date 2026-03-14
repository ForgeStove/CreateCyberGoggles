package io.github.forgestove.create_cyber_goggles.core.util;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
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
	public ClientFluidListTooltipComponent(List<FluidStack> fluids, int maxColumns, int indent) {
		this.fluids = fluids;
		this.maxColumns = maxColumns;
		this.columns = Math.min(fluids.size(), maxColumns);
		this.rows = (fluids.size() + maxColumns - 1) / maxColumns;
		this.indent = Math.max(0, indent);
	}
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(
			FluidListTooltipComponent.class,
			data -> new ClientFluidListTooltipComponent(data.fluids(), data.maxColumns(), data.indent())
		);
	}
	public static void renderFluidBar(@NotNull GuiGraphics guiGraphics, @NotNull FluidStack stack, int x, int y, int width, int height) {
		guiGraphics.blit(ClientItemListTooltipComponent.SLOT_SPRITE, x, y, 0, 0, SLOT_WIDTH, SLOT_HEIGHT);
		if (stack.isEmpty()) return;
		var innerX = x + 1;
		var innerY = y + 1;
		var innerW = Math.max(0, width - 2);
		var innerH = Math.max(0, height - 2);
		renderFluid(guiGraphics, stack, innerX, innerY, innerW, innerH);
	}
	private static void renderFluid(@NotNull GuiGraphics guiGraphics, @NotNull FluidStack stack, int x, int y, int width, int height) {
		if (stack.isEmpty()) return;
		var ext = IClientFluidTypeExtensions.of(stack.getFluid());
		var still = ext.getStillTexture(stack);
		var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
		var tint = ext.getTintColor(stack);
		var r = FastColor.ARGB32.red(tint) / 255F;
		var g = FastColor.ARGB32.green(tint) / 255F;
		var b = FastColor.ARGB32.blue(tint) / 255F;
		var a = FastColor.ARGB32.alpha(tint) / 255F;
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(r, g, b, a);
		for (var dx = 0; dx < width; dx += 16) {
			var sliceWidth = Math.min(16, width - dx);
			if (sliceWidth <= 0) break;
			guiGraphics.blit(x + dx, y, 0, sliceWidth, height, sprite);
		}
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
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
}
