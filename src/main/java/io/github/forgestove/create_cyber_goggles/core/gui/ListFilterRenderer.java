package io.github.forgestove.create_cyber_goggles.core.gui;
import com.zurrtum.create.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
public class ListFilterRenderer extends AbstractItemGridRenderer {
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.listFilter && stack.is(AllItems.FILTER);
	}
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		var contents = stack.getOrDefault(AllDataComponents.FILTER_ITEMS, ItemContainerContents.EMPTY);
		var stacks = new ArrayList<ItemStack>();
		for (var itemStack : contents.nonEmptyItems()) stacks.add(itemStack);
		return stacks.isEmpty() ? null : new OverlayData(stacks, 9);
	}
}
