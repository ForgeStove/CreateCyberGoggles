package io.github.forgestove.create_cyber_goggles.mixin;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.world.*;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = ContraptionHandlerClient.class, remap = false)
public abstract class ContraptionHandlerClientMixin {
	@Inject(method = "rightClickingOnContraptionsGetsHandledLocally", at = @At("TAIL"), cancellable = true)
	private static void rightClickingOnContraptionsGetsHandledLocally(
		Minecraft mc,
		HitResult result,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> returnable
	) {
		if (!CCG.CONFIG.other.rightClickPenetrate) return;
		returnable.setReturnValue(InteractionResult.PASS);
	}
}
