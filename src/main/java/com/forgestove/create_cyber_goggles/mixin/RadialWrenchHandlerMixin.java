package com.forgestove.create_cyber_goggles.mixin;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchHandler;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RadialWrenchHandler.class)
public abstract class RadialWrenchHandlerMixin {
	@Shadow(remap = false) public static int COOLDOWN;
	@Redirect(
		method = "onKeyInput", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"
	), remap = false
	)
	private static GameType redirectPlayerMode(MultiPlayerGameMode instance) {
		return CCGConfig.get().wrench.alwaysAllowRotating ? null : instance.getPlayerMode();
	}
	@Redirect(
		method = "onKeyInput", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"
	), remap = false
	)
	private static Item redirectMainHandItem(ItemStack instance) {
		return CCGConfig.get().wrench.alwaysAllowRotating ? AllItems.WRENCH.get() : instance.getItem();
	}
	@Inject(method = "clientTick", at = @At("HEAD"), remap = false, cancellable = true)
	private static void clientTick(CallbackInfo callbackInfo) {
		if (!CCGConfig.get().wrench.removeCooldown) return;
		callbackInfo.cancel();
		COOLDOWN = 0;
	}
}
