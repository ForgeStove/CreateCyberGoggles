package com.ForgeStove.create_cyber_goggles.mixin;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(CogWheelBlock.class) public abstract class CogWheelBlockMixin {
	@Inject(method = "isValidCogwheelPosition", at = @At("HEAD"), cancellable = true)
	private static void isValidCogwheelPosition(CallbackInfoReturnable<Boolean> returnable) {
		returnable.setReturnValue(true);
	}
}