package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(ChainConveyorRidingHandler.class)
public abstract class ChainConveyorRidingHandlerMixin {
	@WrapOperation(
		method = "clientTick", at = @At(
		value = "INVOKE", target = "Lcom/simibubi/create/AllTags$AllItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z"
	)
	)
	private static boolean wrapChainRideableCheck(AllItemTags instance, ItemStack stack, Operation<Boolean> original) {
		if (CCG.CONFIG.chainConveyor.preventFalling) ChainConveyorRidingHandler.catchingUp = 20;
		return CCG.CONFIG.chainConveyor.alwaysAllowRiding || original.call(instance, stack);
	}
	@Inject(method = "clientTick", at = @At(value = "TAIL"))
	private static void injectTail(CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.chainConveyor.cardBoardedYourself) return;
		if (testForStealth()) sendAction(Action.PRESS_SHIFT_KEY);
	}
	@Inject(method = "stopRiding", at = @At("HEAD"))
	private static void stopRiding(CallbackInfo callbackInfo) {
		sendAction(Action.RELEASE_SHIFT_KEY);
	}
}
