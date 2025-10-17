package io.github.forgestove.create_cyber_goggles.mixin.catnip;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.config.ui.*;
import net.minecraft.network.chat.*;
import net.neoforged.fml.config.ModConfig.Type;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(SubMenuConfigScreen.class)
public abstract class SubMenuConfigScreenMixin {
	@Shadow @Final public Type type;
	@WrapOperation(
		method = "init", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"
	)
	)
	private MutableComponent init(String text, Operation<MutableComponent> original) {
		if (!CCG.CONFIG.misc.translateCatnip) return original.call(text);
		return CCGLang.translate(text).component();
	}
	@ModifyArg(
		method = "showLeavingPrompt", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/network/chat/FormattedText;of(Ljava/lang/String;)Lnet/minecraft/network/chat/FormattedText;"
	)
	)
	private String showLeavingPrompt(String text) {
		if (!CCG.CONFIG.misc.translateCatnip) return text;
		var size = ConfigHelper.changes.size();
		return CCGLang.translate("Leaving with %1$d unsaved change%2$s for this config", size, size != 1 ? "s" : "").string();
	}
	@WrapOperation(
		method = "addAnnotationsToConfirm", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/network/chat/FormattedText;of(Ljava/lang/String;)Lnet/minecraft/network/chat/FormattedText;"
	)
	)
	private FormattedText addAnnotationsToConfirm(String text, Operation<FormattedText> original) {
		if (!CCG.CONFIG.misc.translateCatnip || text.isBlank()) return original.call(text);
		return CCGLang.translate(text).component();
	}
	@ModifyArg(
		method = "init",
		at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/config/ui/HintableTextFieldWidget;setHint(Ljava/lang/String;)V")
	)
	private String setHint(String text) {
		if (!CCG.CONFIG.misc.translateCatnip) return text;
		return CCGLang.translate("Ctrl + F to Search...").string();
	}
	@ModifyArg(
		method = "lambda$init$3", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/network/chat/FormattedText;of(Ljava/lang/String;)Lnet/minecraft/network/chat/FormattedText;"
	)
	)
	private String lambda$init$3(String text) {
		if (!CCG.CONFIG.misc.translateCatnip) return text;
		return CCGLang.translate("Resetting all settings of the %1$s config. Are you sure?", type.toString()).string();
	}
	@ModifyArg(
		method = "lambda$init$5", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/network/chat/FormattedText;of(Ljava/lang/String;)Lnet/minecraft/network/chat/FormattedText;"
	)
	)
	private String lambda$init$5(String text) {
		if (!CCG.CONFIG.misc.translateCatnip) return text;
		var size = ConfigHelper.changes.size();
		return CCGLang.translate("Saving %1$d changed value%2$s", size, size != 1 ? "s" : "").string();
	}
	@ModifyArg(
		method = "lambda$init$7", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/network/chat/FormattedText;of(Ljava/lang/String;)Lnet/minecraft/network/chat/FormattedText;"
	)
	)
	private String lambda$init$7(String text) {
		if (!CCG.CONFIG.misc.translateCatnip) return text;
		var size = ConfigHelper.changes.size();
		return CCGLang.translate("Discarding %1$d unsaved change%2$s", size, size != 1 ? "s" : "").string();
	}
}
