package com.forgestove.create_cyber_goggles.mixin.armor;
import com.forgestove.create_cyber_goggles.CCG;
import com.simibubi.create.content.equipment.armor.CardboardArmorStealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value = CardboardArmorStealthOverlay.class, remap = false)
public abstract class CardboardArmorStealthOverlayMixin {
	@Inject(method = "renderHelmetOverlay", at = @At("HEAD"), cancellable = true)
	private void renderHelmetOverlay(CallbackInfo callbackInfo) {
		if (CCG.CONFIG.armor.removeBoxOverlay) callbackInfo.cancel();
	}
}
