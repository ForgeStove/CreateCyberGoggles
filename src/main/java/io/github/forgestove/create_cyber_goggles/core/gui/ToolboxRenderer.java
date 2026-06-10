package io.github.forgestove.create_cyber_goggles.core.gui;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ToolboxRenderer extends AbstractItemGridRenderer {
	public static ItemStack readToolboxFilter(ToolboxInventory inventory, int compartment) {
		if (compartment < 0 || compartment >= inventory.filters.size()) return ItemStack.EMPTY;
		return inventory.filters.get(compartment);
	}
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.toolbox && stack.is(AllItemTags.TOOLBOXES);
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		var inventory = stack.getComponents().get(AllDataComponents.TOOLBOX_INVENTORY);
		if (inventory == null) return null;
		var compartments = 8;
		var stacksPerCompartment = ToolboxInventory.STACKS_PER_COMPARTMENT;
		List<ItemStack> items = new ArrayList<>();
		Set<Integer> zeroCountSlots = new HashSet<>();
		for (var compartment = 0; compartment < compartments; compartment++) {
			var baseIndex = compartment * stacksPerCompartment;
			var consolidated = ItemStack.EMPTY;
			var totalCount = 0;
			for (var offset = 0; offset < stacksPerCompartment; offset++) {
				var slotIndex = baseIndex + offset;
				if (slotIndex >= inventory.getContainerSize()) break;
				var slot = inventory.getItem(slotIndex);
				if (slot.isEmpty()) continue;
				if (consolidated.isEmpty()) {
					consolidated = slot.copyWithCount(1);
					totalCount = slot.getCount();
				} else if (ItemStack.isSameItemSameComponents(consolidated, slot)) totalCount += slot.getCount();
			}
			if (consolidated.isEmpty()) {
				var filter = readToolboxFilter(inventory, compartment);
				if (filter.isEmpty()) {
					items.add(ItemStack.EMPTY);
					continue;
				}
				items.add(filter.copyWithCount(1));
				zeroCountSlots.add(items.size() - 1);
				continue;
			}
			items.add(consolidated.copyWithCount(totalCount));
		}
		if (items.isEmpty() || items.stream().allMatch(ItemStack::isEmpty)) return null;
		return new OverlayData(items, 4, zeroCountSlots);
	}
}
