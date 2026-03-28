package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.client.Minecraft;
import net.minecraft.world.*;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ContraptionHandlerClient.class)
public abstract class ContraptionHandlerClientMixin {
	@Inject(method = "rightClickingOnContraptionsGetsHandledLocally", at = @At("HEAD"), cancellable = true)
	private static void rightClickingOnContraptionsGetsHandledLocally(
		Minecraft mc,
		HitResult result,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		if (!CCGKey.clickPenetrate.keyMapping.isDown()) return;
		if (mc.player == null) return;
		cir.setReturnValue(InteractionResult.PASS);
	}
}
