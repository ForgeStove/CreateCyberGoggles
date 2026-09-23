package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.*;
@Mixin(MillstoneBlockEntity.class)
public abstract class MillstoneBlockEntityMixin extends KineticBlockEntity
	implements IHaveGoggleInformation, ItemRenderable, Self<MillstoneBlockEntity> {
	public MillstoneBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltip.dispatch(thiz(), tooltip, isPlayerSneaking, () -> super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
	@Override
	public ItemStack ccg$getItemStack() {
		var thiz = thiz();
		var input = thiz.inputInv.getStackInSlot(0);
		if (!input.isEmpty()) return input;
		var inventory = thiz.outputInv;
		for (var i = 0; i < inventory.getSlots(); i++) {
			var stackInSlot = inventory.getStackInSlot(i);
			if (!stackInSlot.isEmpty()) return stackInSlot;
		}
		return null;
	}
}
