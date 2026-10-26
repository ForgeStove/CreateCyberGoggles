package io.github.forgestove.create_cyber_goggles.tooltipRenderer;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import io.github.forgestove.create_cyber_goggles.compat.fluidlogistics.PackageTankHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
@AutoTooltipRenderer
public final class PackageItemRenderer extends AbstractItemGridRenderer {
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.packageItem && stack.getItem() instanceof PackageItem && stack.has(AllDataComponents.PACKAGE_CONTENTS);
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		ItemStackHandler contents = PackageItem.getContents(stack);
		List<ItemStack> items = new ArrayList<>();
		for (var i = 0; i < contents.getSlots(); i++) {
			ItemStack itemstack = contents.getStackInSlot(i);
			if (itemstack.isEmpty()) continue;
			int amount = PackageTankHelper.getCFLTankAmount(itemstack);
			items.add(amount > 0 ? itemstack.copyWithCount(amount) : itemstack);
		}
		return items.isEmpty() ? null : new OverlayData(items, 3);
	}
}
