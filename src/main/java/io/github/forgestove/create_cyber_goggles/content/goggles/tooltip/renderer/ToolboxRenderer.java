package io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.renderer;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;
@AutoTooltipRenderer
public final class ToolboxRenderer extends AbstractItemGridRenderer {
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.toolbox && AllItemTags.TOOLBOXES.matches(stack);
	}
	private static ItemStack readToolboxFilter(CompoundTag tag, RegistryAccess access, int compartment) {
		if (!tag.contains("Compartments", 9)) return ItemStack.EMPTY;
		ListTag compartmentsTag = tag.getList("Compartments", 10);
		if (compartment < 0 || compartment >= compartmentsTag.size()) return ItemStack.EMPTY;
		CompoundTag filterTag = compartmentsTag.getCompound(compartment);
		if (filterTag.isEmpty() || !filterTag.contains("id", 8)) return ItemStack.EMPTY;
		return ItemStack.parse(access, filterTag).orElse(ItemStack.EMPTY);
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		ToolboxInventory inventory = stack.getComponents().get(AllDataComponents.TOOLBOX_INVENTORY);
		if (inventory == null) return null;
		// 序列化是整包深拷贝，放进隔间循环会重复 8 次
		RegistryAccess access = mc.level == null ? null : mc.level.registryAccess();
		CompoundTag tag = access == null ? null : inventory.serializeNBT(access);
		var compartments = 8;
		int stacksPerCompartment = ToolboxInventory.STACKS_PER_COMPARTMENT;
		List<ItemStack> items = new ArrayList<>();
		Set<Integer> zeroCountSlots = new HashSet<>();
		for (var compartment = 0; compartment < compartments; compartment++) {
			int baseIndex = compartment * stacksPerCompartment;
			ItemStack consolidated = ItemStack.EMPTY;
			var totalCount = 0;
			for (var offset = 0; offset < stacksPerCompartment; offset++) {
				int slotIndex = baseIndex + offset;
				if (slotIndex >= inventory.getSlots()) break;
				ItemStack slot = inventory.getStackInSlot(slotIndex);
				if (slot.isEmpty()) continue;
				if (consolidated.isEmpty()) {
					consolidated = slot.copyWithCount(1);
					totalCount = slot.getCount();
				} else if (ItemStack.isSameItemSameComponents(consolidated, slot)) totalCount += slot.getCount();
			}
			if (consolidated.isEmpty()) {
				ItemStack filter = tag == null ? ItemStack.EMPTY : readToolboxFilter(tag, access, compartment);
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
