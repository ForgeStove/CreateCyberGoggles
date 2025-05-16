package com.forgestove.create_cyber_goggles.mixin.render;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import dev.engine_room.flywheel.lib.backend.SimpleBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SimpleBackend.class)
public abstract class SimpleBackendMixin {
	@Inject(method = "isSupported", at = @At("HEAD"), cancellable = true)
	private void isSupported(CallbackInfoReturnable<Boolean> returnable) {
		if (CCGConfig.get().other.forcedBackend) returnable.setReturnValue(true);
	}
}
