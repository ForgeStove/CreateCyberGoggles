package io.github.forgestove.create_cyber_goggles.mixin.misc.contraption;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.foundation.util.Self;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerViewRotationMixin implements Self<LocalPlayer> {
	@Inject(method = "getViewYRot", at = @At("HEAD"), cancellable = true)
	private void interpolateViewYaw(float partialTick, CallbackInfoReturnable<Float> cir) {
		if (!CCG.config.misc.followContraptionRotation) return;
		var player = thiz();
		if (player.isPassenger()) return;
		cir.setReturnValue(partialTick == 1.0F ? player.getYRot() : Mth.rotLerp(partialTick, player.yRotO, player.getYRot()));
	}
}
