package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
public record FluidListTooltipComponent(List<FluidStack> fluids, int maxColumns, int indent) implements TooltipComponent {}
