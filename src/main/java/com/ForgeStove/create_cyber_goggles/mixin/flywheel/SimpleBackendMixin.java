package com.ForgeStove.create_cyber_goggles.mixin.flywheel;
import com.ForgeStove.create_cyber_goggles.config.Configs;
import dev.engine_room.flywheel.lib.backend.SimpleBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SimpleBackend.class) public abstract class SimpleBackendMixin {
	@Inject(method = "isSupported", at = @At("HEAD"), cancellable = true)
	private void isSupported(CallbackInfoReturnable<Boolean> returnable) {
		if (Configs.client().forcedBackend.get()) returnable.setReturnValue(true);
	}
}
