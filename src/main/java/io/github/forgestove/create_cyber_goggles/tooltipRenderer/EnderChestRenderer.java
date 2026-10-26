package io.github.forgestove.create_cyber_goggles.tooltipRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import io.github.forgestove.create_cyber_goggles.util.EnderChestTooltipUtil;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
@AutoTooltipRenderer
public final class EnderChestRenderer extends AbstractItemGridRenderer {
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.enderChest && stack.is(Items.ENDER_CHEST);
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		List<ItemStack> items = EnderChestTooltipUtil.cachedItems;
		for (ItemStack item : items) {
			if (item.isEmpty()) continue;
			return new OverlayData(items, 9);
		}
		return null;
	}
}
