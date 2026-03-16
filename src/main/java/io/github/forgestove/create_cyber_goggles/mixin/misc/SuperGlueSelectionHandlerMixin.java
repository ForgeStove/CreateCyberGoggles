package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Debug(export = true)
@Mixin(value = SuperGlueSelectionHandler.class, remap = false)
public abstract class SuperGlueSelectionHandlerMixin {
	@WrapWithCondition(
		method = "tick",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueSelectionHandler;discard()V")
	)
	private static boolean preventDiscard(SuperGlueSelectionHandler instance) {
		return !CCG.config.misc.preventSelectionDiscard;
	}
	@Inject(method = "isGlue", at = @At("HEAD"), cancellable = true)
	private void isGlue(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!CCGKey.showSuperGlue.isDown()) return;
		cir.setReturnValue(true);
	}
}
