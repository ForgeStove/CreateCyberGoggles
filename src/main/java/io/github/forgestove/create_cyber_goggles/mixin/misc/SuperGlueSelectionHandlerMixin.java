package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(SuperGlueSelectionHandler.class)
public abstract class SuperGlueSelectionHandlerMixin {
	@WrapWithCondition(
		method = "tick",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueSelectionHandler;discard()V")
	)
	private static boolean tick(SuperGlueSelectionHandler instance) {
		return !CCG.CONFIG.misc.preventSelectionDiscard;
	}
}
