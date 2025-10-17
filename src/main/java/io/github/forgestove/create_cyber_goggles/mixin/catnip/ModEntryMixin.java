package io.github.forgestove.create_cyber_goggles.mixin.catnip;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.config.ui.ConfigModListScreen.ModEntry;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ModEntry.class)
public abstract class ModEntryMixin {
	@WrapOperation(
		method = "<init>", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
		ordinal = 1
	)
	)
	private MutableComponent init(String text, Operation<MutableComponent> original) {
		if (!CCG.CONFIG.misc.translateCatnip) return original.call(text);
		return CCGLang.translate(text).component();
	}
}
