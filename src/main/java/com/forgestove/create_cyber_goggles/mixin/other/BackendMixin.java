package com.forgestove.create_cyber_goggles.mixin.other;
import com.forgestove.create_cyber_goggles.content.config.*;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.config.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Backend.class)
public abstract class BackendMixin {
	@Inject(method = "chooseEngine", at = @At("HEAD"), remap = false, cancellable = true)
	private static void chooseEngine(CallbackInfoReturnable<BackendType> returnable) {
		if (CCGConfig.config.other.forcedBackend) returnable.setReturnValue(FlwConfig.get().getBackendType());
	}
}
