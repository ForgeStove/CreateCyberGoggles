package io.github.forgestove.create_cyber_goggles.mixin.catnip;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.gui.ConfirmationScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ConfirmationScreen.class)
public abstract class ConfirmationScreenMixin {
	@ModifyArg(
		method = "init", at = @At(
		value = "INVOKE",
		target = "Lnet/createmod/catnip/gui/element/TextStencilElement;<init>(Lnet/minecraft/client/gui/Font;Ljava/lang/String;)V"
	)
	)
	private static String init(String text) {
		if (!CCG.CONFIG.misc.translateCatnip) return text;
		return CCGLang.translate(text).string();
	}
}
