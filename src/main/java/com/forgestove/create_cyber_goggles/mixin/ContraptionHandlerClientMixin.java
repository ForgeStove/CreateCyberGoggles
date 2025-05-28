package com.forgestove.create_cyber_goggles.mixin;
import com.forgestove.create_cyber_goggles.CCG;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value = ContraptionHandlerClient.class, remap = false)
public abstract class ContraptionHandlerClientMixin {
	@Inject(method = "rightClickingOnContraptionsGetsHandledLocally", at = @At("TAIL"))
	private static void rightClickingOnContraptionsGetsHandledLocally(InteractionKeyMappingTriggered event, CallbackInfo ci) {
		if (!CCG.CONFIG.other.rightClickPenetrate) return;
		event.setCanceled(false);
		event.setSwingHand(true);
	}
}
