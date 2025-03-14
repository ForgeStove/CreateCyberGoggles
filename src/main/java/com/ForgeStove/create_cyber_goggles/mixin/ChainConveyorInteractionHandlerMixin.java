package com.ForgeStove.create_cyber_goggles.mixin;
import com.ForgeStove.create_cyber_goggles.Config;
import com.simibubi.create.*;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.*;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorInteractionHandler.*;
@Mixin(ChainConveyorInteractionHandler.class) public abstract class ChainConveyorInteractionHandlerMixin {
	@Shadow public static BlockPos selectedConnection;
	@Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
	private static void isActive(CallbackInfoReturnable<Boolean> returnable) {
		if (!Config.AllowEmptyHandToRideChainConveyor.get()) return;
		returnable.setReturnValue(false);
		LocalPlayer localPlayer = Minecraft.getInstance().player;
		Minecraft mc = Minecraft.getInstance();
		if (localPlayer == null) return;
		ItemStack mainHandItem = localPlayer.getMainHandItem();
		if (mc.level == null
				|| mc.hitResult == null
				|| mc.hitResult instanceof BlockHitResult blockHitResult
				&& mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof ChainConveyorBlock
				&& (
				localPlayer.isShiftKeyDown()
						|| mainHandItem.getItem().equals(Items.CHAIN)
						|| AllBlocks.CHAIN_CONVEYOR.isIn(mainHandItem)
		)) return;
		returnable.setReturnValue(true);
	}
	@Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
	private static void onUse(CallbackInfoReturnable<Boolean> returnable) {
		if (!Config.AllowEmptyHandToRideChainConveyor.get()) return;
		if (selectedLift == null) {
			returnable.setReturnValue(false);
			return;
		}
		returnable.setReturnValue(true);
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		ItemStack mainHandItem = player.getMainHandItem();
		if (AllBlocks.PACKAGE_FROGPORT.isIn(mainHandItem)) {
			PackagePortTargetSelectionHandler.exactPositionOfTarget = selectedBakedPosition;
			PackagePortTargetSelectionHandler.activePackageTarget =
					new PackagePortTarget.ChainConveyorFrogportTarget(selectedLift,
					selectedChainPosition,
					selectedConnection,
					false
			);
			return;
		}
		if (PackageItem.isPackage(mainHandItem)) {
			CatnipServices.NETWORK.sendToServer(new ChainPackageInteractionPacket(
					selectedLift,
					selectedConnection,
					selectedChainPosition,
					mainHandItem
			));
			return;
		}
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
