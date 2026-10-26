package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
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
	private final FluidListTooltipComponent c;
	private final int columns;
	private final int rows;
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(FluidListTooltipComponent.class, ClientFluidListTooltipComponent::new);
	}
	public ClientFluidListTooltipComponent(FluidListTooltipComponent c) {
		this.c = c;
		columns = Math.min(c.fluids.size(), c.maxColumns);
		rows = (c.fluids.size() + c.maxColumns - 1) / c.maxColumns;
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
		return c.indent * font.width(" ");
	}
	public static void renderFluidBar(@NotNull GuiGraphics gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		gui.blitSprite(SlotUtil.SLOT, x, y, 0, SlotUtil.SIZE, SlotUtil.SIZE + 2);
		int innerX = x + 1;
		int innerY = y + 1;
		int innerW = Math.max(0, width - 2);
		int innerH = Math.max(0, height - 2);
		renderFluid(gui, stack, innerX, innerY, innerW, innerH);
	}
	private static void renderFluidCount(@NotNull GuiGraphics gui, @NotNull Font font, @NotNull FluidStack stack, int x, int y) {
		String text = AmountUtil.formatFluidAmount(stack.getAmount());
		gui.drawString(font, text, x + SlotUtil.SIZE - font.width(text), y + SlotUtil.SIZE - font.lineHeight, 0xFFFFFF, true);
	}
	private static void renderFluid(@NotNull GuiGraphics gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(stack.getFluid());
		ResourceLocation still = ext.getStillTexture(stack);
		TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
		int tint = ext.getTintColor(stack);
		float r = ARGB32.red(tint) / 255F;
		float g = ARGB32.green(tint) / 255F;
		float b = ARGB32.blue(tint) / 255F;
		float a = ARGB32.alpha(tint) / 255F;
		for (var dx = 0; dx < width; dx += 16) {
			int sliceWidth = Math.min(16, width - dx);
			gui.blit(x + dx, y, 0, sliceWidth, height, sprite, r, g, b, a);
		}
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
		for (var i = 0; i < c.fluids.size(); i++) {
			FluidStack stack = c.fluids.get(i);
			if (stack.isEmpty()) return;
			int col = i % c.maxColumns;
			int row = i / c.maxColumns;
			int slotX = x + col * SlotUtil.SIZE + indentPixels(font);
			int slotY = y + row * SlotUtil.SIZE;
			renderFluidBar(gui, stack, slotX, slotY, SlotUtil.SIZE, SlotUtil.SIZE);
			renderFluidCount(gui, font, stack, slotX, slotY);
		}
	}
	public record FluidListTooltipComponent(List<FluidStack> fluids, int indent, int maxColumns) implements TooltipComponent {}
}
