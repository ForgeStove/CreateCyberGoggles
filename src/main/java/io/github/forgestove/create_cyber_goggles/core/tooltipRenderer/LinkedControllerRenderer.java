package io.github.forgestove.create_cyber_goggles.core.tooltipRenderer;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
@AutoTooltipRenderer
public final class LinkedControllerRenderer extends AbstractItemGridRenderer {
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.linkedController && stack.getItem() instanceof LinkedControllerItem;
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		var frequencyItems = LinkedControllerItem.getFrequencyItems(stack);
		var items = new ArrayList<ItemStack>(12);
		var hasAnyItem = false;
		for (var row = 0; row < 2; row++)
			for (var column = 0; column < 6; column++) {
				var slotIndex = column * 2 + row;
				var slot = frequencyItems.getStackInSlot(slotIndex);
				if (!slot.isEmpty()) hasAnyItem = true;
				items.add(slot.copyWithCount(1));
			}
		return hasAnyItem ? new OverlayData(items, 6) : null;
	}
}
