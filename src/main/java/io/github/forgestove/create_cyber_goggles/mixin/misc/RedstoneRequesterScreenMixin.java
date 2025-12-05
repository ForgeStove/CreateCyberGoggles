package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin {
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 256))
	private int modifyMaxScrollAmount(int original) {
		return CCG.CONFIG.misc.removeRequestLimit ? Integer.MAX_VALUE : original;
	}
}
