package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import java.util.List;
public class MillstoneTooltipBehavior extends KineticTooltipBehaviour<MillstoneBlockEntity> implements IHaveGoggleInformation {
	public MillstoneTooltipBehavior(MillstoneBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		var sup = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		var level = blockEntity.getLevel();
		if (level == null) return sup;
		var stack = blockEntity.capability.getItem(0);
		if (stack.isEmpty()) return sup;
		var input = new SingleRecipeInput(stack);
		var recipe = level.recipeAccess().getSynchronizedRecipes().getFirstMatch(AllRecipeTypes.MILLING, input, level);
		if (recipe.isEmpty()) return sup;
		var thiz = GoggleTooltipUtil.millstone(tooltip, blockEntity, recipe.get().value());
		return thiz || sup;
	}
}
