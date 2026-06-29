package io.github.forgestove.create_cyber_goggles.core.tooltipRenderer;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ToolboxRenderer extends AbstractItemGridRenderer {
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.toolbox && AllItemTags.TOOLBOXES.matches(stack);
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
				if (slotIndex >= inventory.getSlots()) break;
				var slot = inventory.getStackInSlot(slotIndex);
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
	public static ItemStack readToolboxFilter(ToolboxInventory inventory, int compartment) {
		if (mc.level == null) return ItemStack.EMPTY;
		var access = mc.level.registryAccess();
		var tag = inventory.serializeNBT(access);
		if (!tag.contains("Compartments", 9)) return ItemStack.EMPTY;
		var compartmentsTag = tag.getList("Compartments", 10);
		if (compartment < 0 || compartment >= compartmentsTag.size()) return ItemStack.EMPTY;
		var filterTag = compartmentsTag.getCompound(compartment);
		if (filterTag.isEmpty() || !filterTag.contains("id", 8)) return ItemStack.EMPTY;
		return ItemStack.parse(access, filterTag).orElse(ItemStack.EMPTY);
	}
}
