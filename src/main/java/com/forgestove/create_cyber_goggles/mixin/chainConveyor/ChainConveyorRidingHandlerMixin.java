package com.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import net.createmod.catnip.animation.AnimationTickHolder;
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
	@Redirect(
		method = "clientTick", at = @At(
		value = "INVOKE", target = "Lcom/simibubi/create/AllTags$AllItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z"
	), remap = false
	)
	private static boolean redirectChainRideableCheck(AllItemTags instance, ItemStack stack) {
		var player = Minecraft.getInstance().player;
		if (player == null) return true;
		return CCGConfig.config.chainConveyor.alwaysAllowRiding || AllItemTags.CHAIN_RIDEABLE.matches(player.getMainHandItem());
	}
	@Inject(
		method = "clientTick", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;length()D"
	), remap = false, cancellable = true
	)
	private static void injectCustomDiffCheck(CallbackInfo callbackInfo, @Local(name = "diff") Vec3 diff) {
		var chainConveyor = CCGConfig.config.chainConveyor;
		if (chainConveyor.preventFalling) callbackInfo.cancel();
		var player = Minecraft.getInstance().player;
		if (player == null) return;
		player.setDeltaMovement(player.getDeltaMovement().scale(0.75).add(diff.scale(0.25)));
		if (AnimationTickHolder.getTicks() % 10 == 0) AllPackets.getChannel().sendToServer(new ServerboundChainConveyorRidingPacket(
			ChainConveyorRidingHandler.ridingChainConveyor,
			false
		));
		if (Common.testForStealth(player)) player.connection.send(new ServerboundPlayerCommandPacket(player, Action.PRESS_SHIFT_KEY));
	}
}
