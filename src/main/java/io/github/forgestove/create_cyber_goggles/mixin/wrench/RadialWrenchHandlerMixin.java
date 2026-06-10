package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.client.content.contraptions.wrench.RadialWrenchHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RadialWrenchHandler.class)
public abstract class RadialWrenchHandlerMixin {
	@WrapOperation(
		method = "onKeyInput", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"
	)
	)
	private static @Nullable GameType wrapPlayerMode(MultiPlayerGameMode instance, Operation<GameType> original) {
		return CCG.config.wrench.alwaysAllowRotating ? null : original.call(instance);
	}
	@WrapOperation(
		method = "onKeyInput",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
	)
	private static boolean wrapMainHandItem(ItemStack instance, Item item, Operation<Boolean> original) {
		return CCG.config.wrench.alwaysAllowRotating || original.call(instance, item);
	}
	@Inject(method = "onKeyInput", at = @At("HEAD"))
	private static void clientTick(CallbackInfo ci) {
		if (!CCG.config.wrench.removeCooldown) return;
		RadialWrenchHandler.COOLDOWN = 0;
	}
}
