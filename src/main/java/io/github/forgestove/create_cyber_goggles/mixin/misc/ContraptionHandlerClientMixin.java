package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ContraptionHandlerClient.class)
public abstract class ContraptionHandlerClientMixin {
	@Inject(method = "rightClickingOnContraptionsGetsHandledLocally", at = @At("HEAD"), cancellable = true)
	private static void rightClickingOnContraptionsGetsHandledLocally(
		Minecraft mc,
		InteractionHand hand,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (!CCGKey.clickPenetrate.keyMapping.isDown()) return;
		if (mc.player == null) return;
		cir.setReturnValue(false);
	}
}
