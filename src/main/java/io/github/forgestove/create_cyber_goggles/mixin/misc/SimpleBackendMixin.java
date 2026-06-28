package io.github.forgestove.create_cyber_goggles.mixin.misc;
import dev.engine_room.flywheel.lib.backend.SimpleBackend;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SimpleBackend.class)
public abstract class SimpleBackendMixin {
	@Inject(method = "isSupported", at = @At("HEAD"), cancellable = true, remap = false)
	public void isSupported(CallbackInfoReturnable<Boolean> cir) {
		try {
			if (CCG.config.misc.forcedBackend) cir.setReturnValue(true);
		} catch (Throwable ignored) {}
	}
}
