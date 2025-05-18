package com.forgestove.create_cyber_goggles.mixin.goggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.foundation.utility.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.NumberFormat;
@Mixin(Lang.class)
public abstract class LangMixin {
	@Inject(method = "number", at = @At("HEAD"), remap = false, cancellable = true)
	private static void number(double d, CallbackInfoReturnable<LangBuilder> returnable) {
		if (!CCGConfig.config.goggles.preciseNumbers) return;
		if (d == (long) d) return;
		var format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(8);
		var formatted = format.format(d).replace("\u00A0", " ");
		returnable.setReturnValue(Lang.builder().text(formatted));
	}
}
