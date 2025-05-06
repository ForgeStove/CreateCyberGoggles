package com.forgestove.create_cyber_goggles.mixin.armor;
import com.forgestove.create_cyber_goggles.content.config.CyberConfig;
import com.simibubi.create.content.equipment.armor.CardboardArmorStealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CardboardArmorStealthOverlay.class)
public abstract class CardboardArmorStealthOverlayMixin {
	@Inject(method = "renderHelmetOverlay", at = @At("HEAD"), remap = false, cancellable = true)
	private void renderHelmetOverlay(CallbackInfo callbackInfo) {
		if (CyberConfig.get().armor.removeBoxOverlay) callbackInfo.cancel();
	}
}
