package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(value = HosePulleyBlockEntity.class, remap = false)
public abstract class HosePulleyBlockEntityMixin {
	@Shadow private boolean infinite;
	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
	public void addToGoggleTooltip(
		List<Component> tooltip,
		boolean isPlayerSneaking,
		CallbackInfoReturnable<Boolean> cir,
		@Local(name = "addToGoggleTooltip") boolean addToGoggleTooltip
	) {
		cir.setReturnValue(addToGoggleTooltip || infinite);
	}
}
