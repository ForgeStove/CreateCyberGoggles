package com.ForgeStove.create_cyber_goggles.mixin;
import com.simibubi.create.content.kinetics.base.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(KineticBlockEntity.class) public abstract class KineticBlockEntityMixin {
	@Shadow(remap = false) protected boolean overStressed;
	@Inject(method = "addToGoggleTooltip", remap = false, at = @At("RETURN"))
	private void addToGoggleTooltip(
			List<Component> tooltip,
			boolean isPlayerSneaking,
			CallbackInfoReturnable<Boolean> returnable
	) {
		IRotate.SpeedLevel.getFormattedSpeedText(getSpeed(), overStressed).forGoggles(tooltip);
	}
	@Shadow(remap = false) public abstract float getSpeed();
}
