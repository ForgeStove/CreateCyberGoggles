package io.github.forgestove.create_cyber_goggles.mixin.catnip;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(FontHelper.class)
public abstract class FontHelperMixin {
	@WrapOperation(
		method = "cutStringTextComponent(Ljava/lang/String;Lnet/createmod/catnip/lang/FontHelper$Palette;)Ljava/util/List;", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"
	)
	)
	private static @NotNull MutableComponent cutStringTextComponent(String text, Operation<MutableComponent> original) {
		if (!CCG.CONFIG.misc.translateCatnip) return original.call(text);
		return CCGLang.translate(text).component();
	}
}
