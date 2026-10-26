package io.github.forgestove.create_cyber_goggles.tooltipRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.Nullable;

import java.util.*;
@AutoTooltipRenderer
public class ContainerRenderer extends AbstractItemGridRenderer {
	private static final int COLUMNS = 9;
	private static boolean isVanilla27Container(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) return true;
		return item == Items.CHEST || item == Items.TRAPPED_CHEST || item == Items.BARREL || item == Items.CHEST_MINECART;
	}
	private static boolean isVanilla9Container(ItemStack stack) {
		Item item = stack.getItem();
		return item == Items.DISPENSER || item == Items.DROPPER || item == Items.HOPPER;
	}
	@Override
	public boolean supports(ItemStack stack) {
		if (!CCG.config.tooltip.container) return false;
		ItemContainerContents container = stack.getComponents().get(DataComponents.CONTAINER);
		if (container == null) return false;
		for (var i = 0; i < container.getSlots(); i++)
			if (!container.getStackInSlot(i).isEmpty()) return true;
		return false;
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		ItemContainerContents container = stack.getComponents().get(DataComponents.CONTAINER);
		if (container == null) return null;
		int storedSlots = container.getSlots();
		if (storedSlots <= 0) return null;
		int slots = isVanilla27Container(stack) ? 27 : storedSlots;
		List<ItemStack> items = new ArrayList<>();
		for (var i = 0; i < slots; i++) items.add(i < storedSlots ? container.getStackInSlot(i) : ItemStack.EMPTY);
		int columns = Math.min(slots, COLUMNS);
		if (isVanilla9Container(stack)) columns = 3;
		return new OverlayData(items, columns);
	}
}
