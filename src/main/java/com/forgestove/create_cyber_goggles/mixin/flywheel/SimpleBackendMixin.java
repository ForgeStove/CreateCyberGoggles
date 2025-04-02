package com.forgestove.create_cyber_goggles.mixin.flywheel;
import com.forgestove.create_cyber_goggles.Config;
import dev.engine_room.flywheel.lib.backend.SimpleBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SimpleBackend.class)
public abstract class SimpleBackendMixin {
	@Inject(method = "isSupported", at = @At("HEAD"), remap = false, cancellable = true)
	private void isSupported(CallbackInfoReturnable<Boolean> returnable) {
		if (Config.forcedBackend.get()) returnable.setReturnValue(true);
	}
}
