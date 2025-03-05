package com.ForgeStove.create_project.mixin;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ChainConveyorConnectionHandler.class) public abstract class ChainConveyorConnectionHandlerMixin {
	@Shadow(remap = false) private static BlockPos firstPos;
	@Shadow(remap = false) private static ResourceKey<Level> firstDim;
	@Inject(method = "validateAndConnect", at = @At("HEAD"), remap = false, cancellable = true)
	private static void validateAndConnect(
			LevelAccessor level,
			BlockPos pos,
			Player player,
			ItemStack chain,
			boolean simulate,
			CallbackInfoReturnable<Boolean> returnable
	) {
		returnable.setReturnValue(false);
		if (!simulate && player.isShiftKeyDown()) {
			CreateLang.translate("chain_conveyor.selection_cleared").sendStatus(player);
			return;
		}
		if (pos.equals(firstPos)) return;
		if (!pos.closerThan(firstPos, AllConfigs.server().kinetics.maxChainConveyorLength.get())) {
			CreateLang.translate("chain_conveyor.too_far").style(ChatFormatting.RED).sendStatus(player);
			return;
		}
		ChainConveyorBlock chainConveyorBlock = AllBlocks.CHAIN_CONVEYOR.get();
		ChainConveyorBlockEntity sourceLift = chainConveyorBlock.getBlockEntity(level, firstPos);
		ChainConveyorBlockEntity targetLift = chainConveyorBlock.getBlockEntity(level, pos);
		if (sourceLift == null || targetLift == null) {
			CreateLang.translate("chain_conveyor.blocks_invalid").style(ChatFormatting.RED).sendStatus(player);
			return;
		}
		if (targetLift.connections.size() >= AllConfigs.server().kinetics.maxChainConveyorConnections.get()) {
			CreateLang.translate("chain_conveyor.cannot_add_more_connections")
					.style(ChatFormatting.RED)
					.sendStatus(player);
			return;
		}
		if (targetLift.connections.contains(firstPos.subtract(pos))) {
			CreateLang.translate("chain_conveyor.already_connected").style(ChatFormatting.RED).sendStatus(player);
			return;
		}
		if (!player.isCreative()) {
			int chainCost = ChainConveyorBlockEntity.getChainCost(pos.subtract(firstPos));
			boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(player, chain, chainCost, true);
			if (simulate) BlueprintOverlayRenderer.displayChainRequirements(chain.getItem(), chainCost, hasEnough);
			if (!hasEnough) {
				CreateLang.translate("chain_conveyor.not_enough_chains").style(ChatFormatting.RED).sendStatus(player);
				return;
			}
		}
		returnable.setReturnValue(true);
		if (simulate) return;
		CatnipServices.NETWORK.sendToServer(new ChainConveyorConnectionPacket(firstPos, pos, chain, true));
		CreateLang.text("").sendStatus(player);
		firstPos = null;
		firstDim = null;
	}
}
