package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.millstone.*;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
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
		var sup = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (level == null) return sup;
		Optional<RecipeHolder<MillingRecipe>> recipe = AllRecipeTypes.MILLING.find(new RecipeWrapper(thiz().inputInv), level);
		if (recipe.isEmpty()) return sup;
		var thiz = GoggleTooltipUtil.millstone(tooltip, thiz(), recipe.get().value());
		return thiz || sup;
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
