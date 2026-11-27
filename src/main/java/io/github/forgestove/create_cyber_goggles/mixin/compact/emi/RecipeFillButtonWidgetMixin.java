package io.github.forgestove.create_cyber_goggles.mixin.compact.emi;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import io.github.forgestove.create_cyber_goggles.compat.emi.RedstoneRequesterEmiHandler;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Pseudo
@Mixin(RecipeFillButtonWidget.class)
public abstract class RecipeFillButtonWidgetMixin {
	@Shadow private boolean canFill;
	@Unique
	private void ccg$refreshCanFill() {
		Boolean state = RedstoneRequesterEmiHandler.fillState(this);
		if (state != null) canFill = state;
	}
	@Inject(method = "getTextureOffset", at = @At("HEAD"))
	private void ccg$refreshBeforeRender(int mouseX, int mouseY, CallbackInfoReturnable<Integer> cir) {
		ccg$refreshCanFill();
	}
	@Inject(method = "getTooltip", at = @At("HEAD"))
	private void ccg$refreshBeforeTooltip(int mouseX, int mouseY, CallbackInfoReturnable<List<ClientTooltipComponent>> cir) {
		ccg$refreshCanFill();
	}
	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void ccg$refreshBeforeClick(int mouseX, int mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		ccg$refreshCanFill();
	}
}
