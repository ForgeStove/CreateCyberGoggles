package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = DivingBootsItem.class, remap = false)
public abstract class DivingBootsItemMixin {
	@Inject(method = "isWornBy", at = @At("HEAD"), cancellable = true)
	private static void isWornBy(CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.CONFIG.misc.allowDivingBoot) cir.setReturnValue(false);
	}
}
