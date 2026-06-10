package io.github.forgestove.create_cyber_goggles.mixin.misc;
import io.github.forgestove.create_cyber_goggles.core.event.ItemTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Shadow protected Slot hoveredSlot;
	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At("HEAD"))
	private void ccg$captureContainerTooltipStack(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
		if (hoveredSlot != null && hoveredSlot.hasItem()) {
			ItemTooltip.capturedStack = hoveredSlot.getItem();
			ItemTooltip.capturedMouseX = x;
			ItemTooltip.capturedMouseY = y;
		}
	}
}
