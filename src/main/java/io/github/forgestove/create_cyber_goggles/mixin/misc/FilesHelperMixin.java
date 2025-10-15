package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.foundation.utility.FilesHelper;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(FilesHelper.class)
public abstract class FilesHelperMixin {
	@Inject(method = "slug", at = @At("HEAD"), cancellable = true)
	private static void slug(String name, CallbackInfoReturnable<String> cir) {
		if (CCG.CONFIG.misc.fixSchematicName) cir.setReturnValue(name.replaceAll("[\\\\/:*?\"<>|]+", "_"));
	}
}
