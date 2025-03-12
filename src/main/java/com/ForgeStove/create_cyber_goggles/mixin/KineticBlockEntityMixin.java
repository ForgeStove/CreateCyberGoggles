package com.ForgeStove.create_cyber_goggles.mixin;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(KineticBlockEntity.class) public abstract class KineticBlockEntityMixin {
	@Shadow protected boolean overStressed;
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	private void addToGoggleTooltip(
			List<Component> tooltip,
			boolean isPlayerSneaking,
			CallbackInfoReturnable<Boolean> returnable
	) {
		returnable.setReturnValue(true);
		CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
		if (IRotate.StressImpact.isEnabled()) {
			float stressAtBase = calculateStressApplied();
			if (!Mth.equal(stressAtBase, 0)) addStressImpactStats(tooltip, stressAtBase);
		}
		IRotate.SpeedLevel.getFormattedSpeedText(getSpeed(), overStressed).forGoggles(tooltip);
	}
	@Shadow public abstract float getSpeed();
	@Shadow public abstract float calculateStressApplied();
	@Shadow protected abstract void addStressImpactStats(List<Component> tooltip, float stressAtBase);
}
