package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value = NetheriteBacktankFirstPersonRenderer.class, remap = false)
public abstract class NetheriteBacktankFirstPersonRendererMixin {
	@Shadow private static boolean rendererActive;
	@Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
	private static void clientTick(CallbackInfo ci) {
		if (!CCG.CONFIG.misc.removeNetheriteFirstPerson) return;
		ci.cancel();
		rendererActive = false;
	}
}
