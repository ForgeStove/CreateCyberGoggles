package io.github.forgestove.create_cyber_goggles.mixin.misc;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EditBox.class)
public abstract class EditBoxMixin {
	@Shadow private int maxLength;
	@Inject(method = "setMaxLength", at = @At("HEAD"), cancellable = true)
	private void setMaxLength(int maxLength, CallbackInfo ci) {
		if (!CCG.config.misc.infEditBoxLength) return;
		ci.cancel();
		this.maxLength = Integer.MAX_VALUE;
	}
}
