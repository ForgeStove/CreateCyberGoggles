package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import io.github.forgestove.create_cyber_goggles.*;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ChainConveyorRidingHandler.class)
public abstract class ChainConveyorRidingHandlerMixin {
	@WrapOperation(
		method = "clientTick", at = @At(
		value = "INVOKE", target = "Lcom/simibubi/create/AllTags$AllItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z"
	)
	)
	private static boolean wrapChainRideableCheck(AllItemTags instance, ItemStack stack, Operation<Boolean> original) {
		return CCG.CONFIG.chainConveyor.alwaysAllowRiding || original.call(instance, stack);
	}
	@Inject(
		method = "clientTick", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;length()D"
	), cancellable = true
	)
	private static void injectCustomDiffCheck(CallbackInfo callbackInfo, @Local(name = "diff") Vec3 diff) {
		var chainConveyor = CCG.CONFIG.chainConveyor;
		if (chainConveyor.preventFalling) callbackInfo.cancel();
		var player = Minecraft.getInstance().player;
		if (player == null) return;
		player.setDeltaMovement(player.getDeltaMovement().scale(0.75).add(diff.scale(0.25)));
		if (AnimationTickHolder.getTicks() % 10 == 0) CatnipServices.NETWORK.sendToServer(new ServerboundChainConveyorRidingPacket(
			ChainConveyorRidingHandler.ridingChainConveyor,
			false
		));
		if (Common.testForStealth(player)) player.connection.send(new ServerboundPlayerCommandPacket(player, Action.PRESS_SHIFT_KEY));
	}
}
