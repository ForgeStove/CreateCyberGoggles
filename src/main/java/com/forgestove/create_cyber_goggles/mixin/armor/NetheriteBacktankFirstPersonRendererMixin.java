package com.forgestove.create_cyber_goggles.mixin.armor;
import com.forgestove.create_cyber_goggles.Config;
import com.simibubi.create.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(NetheriteBacktankFirstPersonRenderer.class)
public class NetheriteBacktankFirstPersonRendererMixin {
	@Shadow(remap = false) private static boolean rendererActive;
	@Inject(method = "clientTick", at = @At("HEAD"), remap = false, cancellable = true)
	private static void clientTick(CallbackInfo callbackInfo) {
		if (!Config.removeNetheriteFirstPerson.get()) return;
		callbackInfo.cancel();
		rendererActive = false;
	}
}
