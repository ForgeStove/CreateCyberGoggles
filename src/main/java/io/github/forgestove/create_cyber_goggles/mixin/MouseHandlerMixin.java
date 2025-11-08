package io.github.forgestove.create_cyber_goggles.mixin;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.forgestove.create_cyber_goggles.core.event.KeyInput;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Inject(
		method = "onScroll", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;"
	), cancellable = true
	)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci, @Local(ordinal = 4) double delta) {
		if (KeyInput.mouseScroll(vertical)) ci.cancel();
	}
}
