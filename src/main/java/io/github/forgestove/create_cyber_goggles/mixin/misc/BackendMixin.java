package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.config.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = Backend.class, remap = false)
public abstract class BackendMixin {
	@Inject(method = "chooseEngine", at = @At("HEAD"), cancellable = true)
	private static void chooseEngine(CallbackInfoReturnable<BackendType> returnable) {
		try {
			if (CCG.CONFIG.misc.forcedBackend) returnable.setReturnValue(FlwConfig.get().getBackendType());
		} catch (Throwable ignored) {}
	}
}
