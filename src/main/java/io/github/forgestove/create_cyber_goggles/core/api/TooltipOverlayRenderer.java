package io.github.forgestove.create_cyber_goggles.core.api;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
public interface TooltipOverlayRenderer {
	boolean supports(ItemStack stack);
	default boolean canRender(ItemStack stack) {
		return true;
	}
	int width(ItemStack stack);
	int height(ItemStack stack);
	void render(GuiGraphicsExtractor gui, ItemStack stack, int x, int y);
}
