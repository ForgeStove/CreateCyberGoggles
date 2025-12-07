package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

import java.util.List;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin {
	@Shadow(remap = false) private List<Integer> amounts;
	@ModifyArg(
		method = "mouseScrolled", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"
	), index = 2
	)
	public int modifyMaxScrollAmount(int max) {
		return CCG.CONFIG.misc.removeRequestLimit ? Integer.MAX_VALUE : max;
	}
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 10))
	public int modifyPerScrollAmount(int original, @Local(name = "i") int i) {
		return CCG.CONFIG.misc.removeRequestLimit ? Item.MAX_STACK_SIZE - (amounts.get(i) == 1 ? 1 : 0) : original;
	}
}
