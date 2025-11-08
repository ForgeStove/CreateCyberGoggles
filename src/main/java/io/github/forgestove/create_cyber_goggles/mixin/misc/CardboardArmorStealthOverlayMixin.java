package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.zurrtum.create.client.content.equipment.armor.CardboardArmorStealthOverlay;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CardboardArmorStealthOverlay.class)
public abstract class CardboardArmorStealthOverlayMixin {
	@Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
	private static void clientTick(CallbackInfo ci) {
		if (CCG.CONFIG.misc.removeCardboardOverlay) ci.cancel();
	}
}
