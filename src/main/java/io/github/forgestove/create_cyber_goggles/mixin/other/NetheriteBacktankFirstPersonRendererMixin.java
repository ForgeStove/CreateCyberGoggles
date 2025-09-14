package io.github.forgestove.create_cyber_goggles.mixin.other;
import com.simibubi.create.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(NetheriteBacktankFirstPersonRenderer.class)
public abstract class NetheriteBacktankFirstPersonRendererMixin {
	@Shadow private static boolean rendererActive;
	@Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
	private static void clientTick(CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.other.removeNetheriteFirstPerson) return;
		callbackInfo.cancel();
		rendererActive = false;
	}
}
