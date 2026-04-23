package io.github.forgestove.create_cyber_goggles.core.gui;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.*;
public final class PackageItemRenderer extends AbstractItemGridRenderer {
    @Override
    public boolean supports(ItemStack stack) {
        return CCG.config.tooltip.packageItem && stack.getItem() instanceof PackageItem && stack.has(AllDataComponents.PACKAGE_CONTENTS);
    }
    @Override
    public @Nullable OverlayData buildItemGrid(ItemStack stack) {
        var contents = PackageItem.getContents(stack);
        List<ItemStack> items = new ArrayList<>();
        for (var i = 0; i < contents.getSlots(); i++) {
            var itemstack = contents.getStackInSlot(i);
            if (itemstack.isEmpty()) continue;
            items.add(itemstack);
        }
        return items.isEmpty() ? null : new OverlayData(items, 3);
    }
}
