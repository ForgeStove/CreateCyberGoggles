package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import com.zurrtum.create.infrastructure.packet.c2s.ServerboundChainConveyorRidingPacket;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ChainConveyorRidingHandler.class)
public abstract class ChainConveyorRidingHandlerMixin {
	@WrapOperation(
		method = "clientTick", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"
	)
	)
	private static boolean wrapChainRideableCheck(ItemStack instance, TagKey<Item> tag, Operation<Boolean> original) {
		return CCG.CONFIG.chainConveyor.alwaysAllowRiding || original.call(instance, tag);
	}
	@Inject(
		method = "clientTick", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;length()D"
	), cancellable = true
	)
	private static void injectCustomDiffCheck(CallbackInfo callbackInfo, @Local(name = "diff") Vec3 diff) {
		var chainConveyor = CCG.CONFIG.chainConveyor;
		if (chainConveyor.preventFalling) callbackInfo.cancel();
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		player.setDeltaMovement(player.getDeltaMovement().scale(0.75).add(diff.scale(0.25)));
		if (AnimationTickHolder.getTicks() % 10 == 0)
			player.connection.send(new ServerboundChainConveyorRidingPacket(ChainConveyorRidingHandler.ridingChainConveyor, false));
	}
}
