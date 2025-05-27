package com.forgestove.create_cyber_goggles.mixin.other;
import com.forgestove.create_cyber_goggles.CCG;
import com.forgestove.create_cyber_goggles.content.util.SafeRun;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.config.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = Backend.class, remap = false)
public abstract class BackendMixin {
	@Inject(method = "chooseEngine", at = @At("HEAD"), cancellable = true)
	private static void chooseEngine(CallbackInfoReturnable<BackendType> returnable) {
		SafeRun.run(() -> {if (CCG.CONFIG.other.forcedBackend) returnable.setReturnValue(FlwConfig.get().getBackendType());});
	}
}
