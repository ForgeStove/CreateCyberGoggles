package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(HosePulleyBlockEntity.class)
public abstract class HosePulleyBlockEntityMixin {
	@Shadow private boolean infinite;
	@Shadow private SmartFluidTank internalTank;
	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
	public void addToGoggleTooltip(
		List<Component> tooltip,
		boolean isPlayerSneaking,
		CallbackInfoReturnable<Boolean> cir,
		@Local(name = "addToGoggleTooltip") boolean addToGoggleTooltip
	) {
		CCGLang.fluidEntry(internalTank.getFluid(), internalTank.getCapacity()).forGoggles(tooltip);
		cir.setReturnValue(addToGoggleTooltip || infinite);
	}
}
