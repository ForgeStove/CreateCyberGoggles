package com.ForgeStove.create_project.mixin;
import com.simibubi.create.content.kinetics.base.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(KineticBlockEntity.class) public abstract class KineticBlockEntityMixin {
	@Shadow protected boolean overStressed;
	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"))
	private void addToGoggleTooltip(
			List<Component> tooltip,
			boolean isPlayerSneaking,
			CallbackInfoReturnable<Boolean> returnable
	) {
		IRotate.SpeedLevel.getFormattedSpeedText(getSpeed(), overStressed).forGoggles(tooltip);
	}
	@Shadow public abstract float getSpeed();
}
