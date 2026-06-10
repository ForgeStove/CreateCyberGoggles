package io.github.forgestove.create_cyber_goggles.mixin.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.infrastructure.fluids.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(IHaveGoggleInformation.class)
public interface IHaveGoggleInformationMixin {
	@Inject(method = "containedFluidTooltip", at = @At("HEAD"), cancellable = true)
	private void containedFluidTooltip(
		List<Component> tooltip,
		boolean isPlayerSneaking,
		FluidInventory handler,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (!CCG.config.tooltip.fluidContainer) return;
		if (handler == null || handler.isEmpty()) {
			cir.setReturnValue(false);
			return;
		}
		CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		var isEmpty = true;
		for (var i = 0; i < handler.size(); i++) {
			var fluidStack = handler.getStack(i);
			if (fluidStack.isEmpty()) continue;
			CCGLang.fluidEntry(fluidStack, handler.getMaxAmount(fluidStack)).forGoggles(tooltip);
			isEmpty = false;
		}
		if (handler.size() > 1) {
			if (isEmpty && !tooltip.isEmpty()) tooltip.removeLast();
			cir.setReturnValue(true);
			return;
		}
		if (isEmpty) for (var i = 0; i < handler.size(); i++)
			CCGLang.fluidEntry(FluidStack.EMPTY, handler.getMaxAmount(FluidStack.EMPTY)).forGoggles(tooltip);
		cir.setReturnValue(true);
	}
}
