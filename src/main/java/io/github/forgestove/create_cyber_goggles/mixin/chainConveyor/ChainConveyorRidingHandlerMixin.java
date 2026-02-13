package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(ChainConveyorRidingHandler.class)
public abstract class ChainConveyorRidingHandlerMixin {
	@WrapOperation(
		method = "clientTick", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isHolding(Ljava/util/function/Predicate;)Z"
	)
	)
	private static boolean wrapChainRideableCheck(LocalPlayer instance, Predicate<ItemStack> predicate, Operation<Boolean> original) {
		if (CCG.config.chainConveyor.preventFalling) ChainConveyorRidingHandler.catchingUp = 20;
		return CCG.config.chainConveyor.alwaysAllowRiding || original.call(instance, predicate);
	}
	@Inject(method = "clientTick", at = @At(value = "TAIL"))
	private static void injectTail(CallbackInfo ci) {
		if (!CCG.config.chainConveyor.cardBoardedYourself) return;
		if (testForStealth()) sendAction(Action.PRESS_SHIFT_KEY);
	}
	@Inject(method = "stopRiding", at = @At("HEAD"))
	private static void stopRiding(CallbackInfo ci) {
		sendAction(Action.RELEASE_SHIFT_KEY);
	}
}
