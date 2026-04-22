package io.github.forgestove.create_cyber_goggles.core.gui;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.EnderChestTooltipUtil;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;
public final class EnderChestRenderer extends AbstractItemGridRenderer {
    @Override
    public boolean supports(ItemStack stack) {
        return CCG.config.tooltip.enderChest && stack.is(Items.ENDER_CHEST);
    }
    @Override
    public @Nullable OverlayData buildItemGrid(ItemStack stack) {
        if (!supports(stack)) return null;
        var items = EnderChestTooltipUtil.getCachedItems();
        if (!items.isEmpty()) return new OverlayData(items, 9);
        return null;
    }
}
