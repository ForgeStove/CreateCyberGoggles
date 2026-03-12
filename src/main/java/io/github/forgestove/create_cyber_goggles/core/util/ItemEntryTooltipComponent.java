package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
public record ItemEntryTooltipComponent(ItemStack stack, Component label, int indent) implements TooltipComponent {}

