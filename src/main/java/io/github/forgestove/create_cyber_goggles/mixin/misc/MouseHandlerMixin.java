
package io.github.forgestove.create_cyber_goggles.mixin.misc;

import io.github.forgestove.create_cyber_goggles.core.event.KeyInput;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void ccg$onScroll(long windowPointer, double horizontal, double vertical, CallbackInfo ci) {
		if (windowPointer != mc.getWindow().getWindow()) return;
		if (KeyInput.mouseScroll(vertical)) ci.cancel();
	}
}
