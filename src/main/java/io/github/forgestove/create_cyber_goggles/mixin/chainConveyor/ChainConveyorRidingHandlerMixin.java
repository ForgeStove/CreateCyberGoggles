package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(ChainConveyorRidingHandler.class)
public abstract class ChainConveyorRidingHandlerMixin {
	@WrapOperation(
		method = "clientTick", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"
	)
	)
	private static boolean wrapChainRideableCheck(ItemStack instance, TagKey<Item> tag, Operation<Boolean> original) {
		if (CCG.CONFIG.chainConveyor.preventFalling) ChainConveyorRidingHandler.catchingUp = 20;
		return CCG.CONFIG.chainConveyor.alwaysAllowRiding || original.call(instance, tag);
	}
	@Inject(method = "clientTick", at = @At(value = "TAIL"))
	private static void injectTail(CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.chainConveyor.cardBoardedYourself) return;
		if (testForStealth()) sendShift(true);
	}
	@Inject(method = "stopRiding", at = @At("HEAD"))
	private static void stopRiding(CallbackInfo callbackInfo) {
		sendShift(false);
	}
}
