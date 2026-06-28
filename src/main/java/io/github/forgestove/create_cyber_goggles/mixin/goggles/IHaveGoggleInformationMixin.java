package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(IHaveGoggleInformation.class)
public interface IHaveGoggleInformationMixin {
	@SuppressWarnings("UnstableApiUsage")
	@Inject(method = "containedFluidTooltip", at = @At("HEAD"), cancellable = true, remap = false)
	private void containedFluidTooltip(
		List<Component> tooltip,
		boolean isPlayerSneaking,
		Storage<FluidVariant> handler,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (handler == null) {
			cir.setReturnValue(false);
			return;
		}
		CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		for (var view : handler) {
			var fluidStack = new FluidStack(view);
			CCGLang.fluid(fluidStack, fluidStack.getAmount()).forGoggles(tooltip);
		}
		cir.setReturnValue(true);
	}
}
