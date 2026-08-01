package io.github.forgestove.create_cyber_goggles.core.tooltipRenderer;
import com.simibubi.create.*;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.mixin.accessor.ItemStackHandlerAccessor;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
@AutoTooltipRenderer
public class ListFilterRenderer extends AbstractItemGridRenderer {
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.listFilter && stack.is(AllItems.FILTER);
	}
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		var accessor = (ItemStackHandlerAccessor) getFilterItemHandler(stack);
		var stacks = accessor.create$getStacks();
		var allEmpty = stacks.stream().allMatch(ItemStack::isEmpty);
		return allEmpty ? null : new OverlayData(stacks, 9);
	}
	public ItemStackHandler getFilterItemHandler(ItemStack stack) {
		var newInv = new ItemStackHandler(18);
		var contents = stack.getOrDefault(AllDataComponents.FILTER_ITEMS, ItemContainerContents.EMPTY);
		ItemHelper.fillItemStackHandler(contents, newInv);
		return newInv;
	}
}
