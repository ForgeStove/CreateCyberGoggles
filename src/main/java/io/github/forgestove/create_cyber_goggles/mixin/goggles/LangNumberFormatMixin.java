package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.catnip.lang.LangNumberFormat;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.NumberFormat;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isServer;
@Mixin(LangNumberFormat.class)
public abstract class LangNumberFormatMixin {
	@Inject(method = "format", at = @At("HEAD"), cancellable = true)
	private static void format(double d, CallbackInfoReturnable<String> cir) {
		if (!CCG.config.goggles.preciseNumber) return;
		if (isServer()) return;
		if (d == (long) d) return;
		if (Math.abs(d) < 1E-3) {
			cir.setReturnValue(String.format("%e", d));
			return;
		}
		var format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(CCG.config.goggles.maxFractionDigits);
		cir.setReturnValue(format.format(d));
	}
}
