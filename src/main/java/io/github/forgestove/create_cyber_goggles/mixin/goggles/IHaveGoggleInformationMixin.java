package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(value = IHaveGoggleInformation.class, remap = false)
public interface IHaveGoggleInformationMixin {
	@Inject(method = "containedFluidTooltip", at = @At("HEAD"), cancellable = true)
	private void containedFluidTooltip(
		List<Component> tooltip,
		boolean isPlayerSneaking,
		LazyOptional<IFluidHandler> optional,
		CallbackInfoReturnable<Boolean> cir
	) {
		var resolve = optional.resolve();
		if (resolve.isEmpty()) {
			cir.setReturnValue(false);
			return;
		}
		var handler = resolve.get();
		if (handler.getTanks() == 0) {
			cir.setReturnValue(false);
			return;
		}
		CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		var isEmpty = true;
		for (var i = 0; i < handler.getTanks(); i++) {
			var fluidStack = handler.getFluidInTank(i);
			if (fluidStack.isEmpty()) continue;
			CCGLang.fluid(fluidStack, handler.getTankCapacity(i)).forGoggles(tooltip);
			isEmpty = false;
		}
		if (handler.getTanks() > 1) {
			if (isEmpty && !tooltip.isEmpty()) tooltip.remove(tooltip.size() - 1);
			cir.setReturnValue(true);
			return;
		}
		if (isEmpty) for (var i = 0; i < handler.getTanks(); i++)
			CCGLang.fluid(FluidStack.EMPTY, handler.getTankCapacity(i)).forGoggles(tooltip);
		cir.setReturnValue(true);
	}
}
