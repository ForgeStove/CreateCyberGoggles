package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.zurrtum.create.client.content.contraptions.glue.SuperGlueSelectionHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(SuperGlueSelectionHandler.class)
public abstract class SuperGlueSelectionHandlerMixin {
	@WrapWithCondition(
		method = "tick", at = @At(
		value = "INVOKE",
		target = "Lcom/zurrtum/create/client/content/contraptions/glue/SuperGlueSelectionHandler;discard"
			+ "(Lnet/minecraft/client/player/LocalPlayer;)V"
	)
	)
	private static boolean tick(SuperGlueSelectionHandler instance, LocalPlayer player) {
		return !CCG.config.misc.preventSelectionDiscard;
	}
}
