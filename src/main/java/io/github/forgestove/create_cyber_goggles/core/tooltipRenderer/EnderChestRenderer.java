package io.github.forgestove.create_cyber_goggles.core.tooltipRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import io.github.forgestove.create_cyber_goggles.core.util.EnderChestTooltipUtil;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;
@AutoTooltipRenderer
public final class EnderChestRenderer extends AbstractItemGridRenderer {
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.enderChest && stack.is(Items.ENDER_CHEST);
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		var items = EnderChestTooltipUtil.cachedItems;
		for (var item : items) {
			if (item.isEmpty()) continue;
			return new OverlayData(items, 9);
		}
		return null;
	}
}
