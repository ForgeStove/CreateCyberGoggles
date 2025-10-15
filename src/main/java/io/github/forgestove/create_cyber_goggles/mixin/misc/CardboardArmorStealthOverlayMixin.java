package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.equipment.armor.CardboardArmorStealthOverlay;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value = CardboardArmorStealthOverlay.class, remap = false)
public abstract class CardboardArmorStealthOverlayMixin {
	@Inject(method = "renderHelmetOverlay", at = @At("HEAD"), cancellable = true)
	public void renderHelmetOverlay(CallbackInfo ci) {
		if (CCG.CONFIG.misc.removeCardboardOverlay) ci.cancel();
	}
}
