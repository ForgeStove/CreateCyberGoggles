package com.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.forgestove.create_cyber_goggles.CCG;
import com.forgestove.create_cyber_goggles.util.Common;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.*;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ChainConveyorInteractionHandler.class)
public abstract class ChainConveyorInteractionHandlerMixin {
	@Shadow public static BlockPos selectedConnection, selectedLift;
	@Shadow public static float selectedChainPosition;
	@Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
	private static void isActive(CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.CONFIG.chainConveyor.alwaysAllowRiding) return;
		returnable.setReturnValue(false);
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		var mainHandItem = player.getMainHandItem();
		if (Common.getSelectedB() instanceof ChainConveyorBlock && (
			player.isShiftKeyDown() || mainHandItem.getItem().equals(Items.CHAIN) || AllBlocks.CHAIN_CONVEYOR.isIn(mainHandItem)
		)) return;
		returnable.setReturnValue(true);
	}
	@WrapOperation(
		method = "onUse",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllTags$AllItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z")
	)
	private static boolean onUse(AllItemTags instance, ItemStack stack, Operation<Boolean> original) {
		return !CCG.CONFIG.chainConveyor.alwaysAllowRiding && original.call(instance, stack);
	}
	@Inject(method = "onUse", at = @At("TAIL"))
	private static void injectTail(CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.CONFIG.chainConveyor.alwaysAllowRiding) return;
		var player = Minecraft.getInstance().player;
		if (player == null) return;
		var mainHandItem = player.getMainHandItem();
		if (!player.isShiftKeyDown()) {
			ChainConveyorRidingHandler.embark(selectedLift, selectedChainPosition, selectedConnection);
			return;
		}
		if (selectedConnection == null) return;
		CatnipServices.NETWORK.sendToServer(new ChainConveyorConnectionPacket(
			selectedLift,
			selectedLift.offset(selectedConnection),
			mainHandItem.isEmpty() ? AllItems.WRENCH.asStack() : mainHandItem,
			false
		));
	}
}
