package io.github.forgestove.create_cyber_goggles.mixin.other;
import dev.engine_room.flywheel.lib.backend.SimpleBackend;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.util.SafeRun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SimpleBackend.class)
public abstract class SimpleBackendMixin {
	@Inject(method = "isSupported", at = @At("HEAD"), cancellable = true)
	private void isSupported(CallbackInfoReturnable<Boolean> returnable) {
		SafeRun.run(() -> {if (CCG.CONFIG.other.forcedBackend) returnable.setReturnValue(true);});
	}
}
