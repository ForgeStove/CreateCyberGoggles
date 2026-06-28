package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.lang.LangNumberFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.NumberFormat;
@Mixin(LangNumberFormat.class)
public abstract class LangNumberFormatMixin {
	@Inject(method = "format", at = @At("HEAD"), cancellable = true,remap = false)
	private static void format(double d, CallbackInfoReturnable<String> cir) {
		if (!CCG.config.goggles.preciseNumber) return;
		if (d == (long) d) return;
		if (Math.abs(d) < 1E-3) {
			cir.setReturnValue(String.format("%e", d));
			return;
		}
		var format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(Integer.MAX_VALUE);
		cir.setReturnValue(format.format(d));
	}
}
