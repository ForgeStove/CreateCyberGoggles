package com.forgestove.create_cyber_goggles.mixin.armor;
import com.forgestove.create_cyber_goggles.CCG;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = DivingBootsItem.class, remap = false)
public abstract class DivingBootsItemMixin {
	@Inject(method = "isWornBy", at = @At("HEAD"), cancellable = true)
	private static void isWornBy(CallbackInfoReturnable<Boolean> returnable) {
		if (CCG.CONFIG.armor.removeDivingBootsAffect) returnable.setReturnValue(false);
	}
}
