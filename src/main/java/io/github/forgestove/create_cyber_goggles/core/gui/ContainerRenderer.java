package io.github.forgestove.create_cyber_goggles.core.gui;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.Nullable;

import java.util.*;
public class ContainerRenderer extends AbstractItemGridRenderer {
	private static final int COLUMNS = 9;
	private static boolean isVanilla27Container(ItemStack stack) {
		var item = stack.getItem();
		if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) return true;
		return item == Items.CHEST || item == Items.TRAPPED_CHEST || item == Items.BARREL || item == Items.CHEST_MINECART;
	}
	private static int resolveSlots(ItemStack stack, int storedSlots) {
		return isVanilla27Container(stack) ? 27 : storedSlots;
	}
	@Override
	public boolean supports(ItemStack stack) {
		if (!CCG.config.tooltip.container) return false;
		var container = stack.getComponents().get(DataComponents.CONTAINER);
		if (container == null) return false;
		for (var i = 0; i < container.getSlots(); i++)
			if (!container.getStackInSlot(i).isEmpty()) return true;
		return false;
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		var container = stack.getComponents().get(DataComponents.CONTAINER);
		if (container == null) return null;
		var storedSlots = container.getSlots();
		if (storedSlots <= 0) return null;
		var slots = resolveSlots(stack, storedSlots);
		List<ItemStack> items = new ArrayList<>();
		for (var i = 0; i < slots; i++) items.add(i < storedSlots ? container.getStackInSlot(i) : ItemStack.EMPTY);
		return new OverlayData(items, Math.min(slots, COLUMNS));
	}
}

