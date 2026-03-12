package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
public record ItemListTooltipComponent(List<ItemStack> items, int maxColumns, int indent) implements TooltipComponent {}
