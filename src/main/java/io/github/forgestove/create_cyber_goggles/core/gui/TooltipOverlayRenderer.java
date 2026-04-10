package io.github.forgestove.create_cyber_goggles.core.gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
public interface TooltipOverlayRenderer {
	boolean supports(ItemStack stack);
	int width(ItemStack stack);
	int height(ItemStack stack);
	void render(GuiGraphics graphics, ItemStack stack, int x, int y);
}
