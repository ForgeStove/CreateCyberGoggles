package io.github.forgestove.create_cyber_goggles.mixin.other;
import com.simibubi.create.content.equipment.armor.CardboardArmorStealthOverlay;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CardboardArmorStealthOverlay.class)
public abstract class CardboardArmorStealthOverlayMixin {
	@Inject(method = "renderHelmetOverlay", at = @At("HEAD"), cancellable = true)
	public void renderHelmetOverlay(CallbackInfo callbackInfo) {
		if (CCG.CONFIG.other.removeCardboardOverlay) callbackInfo.cancel();
	}
}
