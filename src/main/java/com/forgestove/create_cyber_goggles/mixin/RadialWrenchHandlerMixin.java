package com.forgestove.create_cyber_goggles.mixin;
import com.forgestove.create_cyber_goggles.CCG;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchHandler;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RadialWrenchHandler.class)
public abstract class RadialWrenchHandlerMixin {
	@WrapOperation(
		method = "onKeyInput", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"
	), remap = false
	)
	private static GameType wrapPlayerMode(MultiPlayerGameMode instance, Operation<GameType> original) {
		return CCG.CONFIG.wrench.alwaysAllowRotating ? null : original.call(instance);
	}
	@WrapOperation(
		method = "onKeyInput", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"
	), remap = false
	)
	private static Item wrapMainHandItem(ItemStack instance, Operation<Item> original) {
		return CCG.CONFIG.wrench.alwaysAllowRotating ? AllItems.WRENCH.get() : original.call(instance);
	}
	@Inject(method = "clientTick", at = @At("HEAD"), remap = false, cancellable = true)
	private static void clientTick(CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.wrench.removeCooldown) return;
		callbackInfo.cancel();
		RadialWrenchHandler.COOLDOWN = 0;
	}
}
