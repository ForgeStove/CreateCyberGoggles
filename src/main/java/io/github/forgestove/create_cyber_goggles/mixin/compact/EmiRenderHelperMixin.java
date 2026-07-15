package io.github.forgestove.create_cyber_goggles.mixin.compact;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ItemCountFontUtil;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Pseudo
@Mixin(EmiRenderHelper.class)
public class EmiRenderHelperMixin {
	@Inject(method = "renderAmount", at = @At("HEAD"), cancellable = true)
	private static void renderAmount(EmiDrawContext context, int x, int y, Component amount, CallbackInfo ci) {
		if (!CCG.config.misc.createStackCount.enableCreateStyleStackCount) return;
		ItemCountFontUtil.renderSizeLabel(context.raw(), mc.font, x, y, ItemCountFontUtil.getStyledAmount(amount));
		ci.cancel();
	}
}
