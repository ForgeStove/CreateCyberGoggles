package io.github.forgestove.create_cyber_goggles.mixin.catnip;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.config.ui.ConfigModListScreen;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ConfigModListScreen.class)
public abstract class ConfigModListScreenMixin {
	@WrapOperation(
		method = "init", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"
	)
	)
	private @NotNull MutableComponent init(String text, Operation<MutableComponent> original) {
		if (!CCG.CONFIG.misc.translateCatnip) return original.call(text);
		return CCGLang.translate(text).component();
	}
}
