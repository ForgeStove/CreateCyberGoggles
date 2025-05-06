package com.forgestove.create_cyber_goggles.mixin.armor;
import com.forgestove.create_cyber_goggles.content.config.CyberConfig;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(DivingBootsItem.class)
public abstract class DivingBootsItemMixin {
	@Inject(method = "isWornBy", at = @At("HEAD"), remap = false, cancellable = true)
	private static void isWornBy(CallbackInfoReturnable<Boolean> returnable) {
		if (CyberConfig.get().armor.removeDivingBootsAffect) returnable.setReturnValue(false);
	}
}
