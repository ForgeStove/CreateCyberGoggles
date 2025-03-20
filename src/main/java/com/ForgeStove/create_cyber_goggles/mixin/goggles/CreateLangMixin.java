package com.ForgeStove.create_cyber_goggles.mixin.goggles;
import com.ForgeStove.create_cyber_goggles.Config;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.NumberFormat;
@Mixin(CreateLang.class) public abstract class CreateLangMixin {
	@Inject(method = "number", at = @At("HEAD"), cancellable = true)
	private static void number(double d, CallbackInfoReturnable<LangBuilder> returnable) {
		if (!Config.preciseNumbers.get()) return;
		if (d == (long) d) return;
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(8);
		String formatted = format.format(d).replace("\u00A0", " ");
		returnable.setReturnValue(CreateLang.builder().text(formatted));
	}
}
