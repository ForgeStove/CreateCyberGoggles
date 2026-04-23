package io.github.forgestove.create_cyber_goggles.mixin.tooltip;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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
		IFluidHandler handler,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (!CCG.config.tooltip.fluidContainer) return;
		if (handler == null || handler.getTanks() == 0) {
			cir.setReturnValue(false);
			return;
		}
		CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		var isEmpty = true;
		for (var i = 0; i < handler.getTanks(); i++) {
			var fluidStack = handler.getFluidInTank(i);
			if (fluidStack.isEmpty()) continue;
			CCGLang.fluidEntry(fluidStack, handler.getTankCapacity(i)).forGoggles(tooltip);
			isEmpty = false;
		}
		if (handler.getTanks() > 1) {
			if (isEmpty && !tooltip.isEmpty()) tooltip.removeLast();
			cir.setReturnValue(true);
			return;
		}
		if (isEmpty) for (var i = 0; i < handler.getTanks(); i++)
			CCGLang.fluidEntry(FluidStack.EMPTY, handler.getTankCapacity(i)).forGoggles(tooltip);
		cir.setReturnValue(true);
	}
}
