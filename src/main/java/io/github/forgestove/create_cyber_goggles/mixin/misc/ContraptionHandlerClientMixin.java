package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(value = ContraptionHandlerClient.class, remap = false)
public abstract class ContraptionHandlerClientMixin {
	@Inject(method = "rightClickingOnContraptionsGetsHandledLocally", at = @At("HEAD"), cancellable = true)
	private static void rightClickingOnContraptionsGetsHandledLocally(InteractionKeyMappingTriggered event, CallbackInfo callbackInfo) {
		if (!CCGKey.clickPenetrate.isDown()) return;
		if (mc.player == null) return;
		event.setCanceled(false);
		event.setSwingHand(true);
		callbackInfo.cancel();
	}
}
