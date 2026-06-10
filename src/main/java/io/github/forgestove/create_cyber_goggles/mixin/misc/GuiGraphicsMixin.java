package io.github.forgestove.create_cyber_goggles.mixin.misc;
import io.github.forgestove.create_cyber_goggles.core.event.ItemTooltip;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
	@Unique private static ItemStack ccg$tooltipStack = ItemStack.EMPTY;
	@Unique private static int ccg$tooltipMouseX;
	@Unique private static int ccg$tooltipMouseY;
	@Inject(
		method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD")
	)
	private void ccg$captureTooltipStack(Font font, ItemStack stack, int x, int y, CallbackInfo ci) {
		ccg$tooltipStack = stack;
		ccg$tooltipMouseX = x;
		ccg$tooltipMouseY = y;
	}
	@Inject(
		method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;"
			+ "IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
		at = @At("TAIL")
	)
	private void ccg$renderTooltipOverlay(
		Font font,
		List<ClientTooltipComponent> components,
		int x,
		int y,
		ClientTooltipPositioner positioner,
		Identifier background,
		CallbackInfo ci
	) {
		ItemTooltip.renderTooltipOverlay(
			ccg$tooltipStack,
			(GuiGraphics) (Object) this,
			font,
			components,
			ccg$tooltipMouseX,
			ccg$tooltipMouseY,
			positioner
		);
		ccg$tooltipStack = ItemStack.EMPTY;
	}
}
