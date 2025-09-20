package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.catnip.lang.LangNumberFormat;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.NumberFormat;
@Mixin(value = LangNumberFormat.class, remap = false)
public abstract class LangNumberFormatMixin {
	@Inject(method = "format", at = @At("HEAD"), cancellable = true)
	private static void format(double d, CallbackInfoReturnable<String> returnable) {
		if (!CCG.CONFIG.goggles.preciseNumber) return;
		if (d == (long) d) return;
		if (Math.abs(d) < 1E-3) {
			returnable.setReturnValue(String.format("%e", d));
			return;
		}
		var format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(Integer.MAX_VALUE);
		returnable.setReturnValue(format.format(d));
	}
}
