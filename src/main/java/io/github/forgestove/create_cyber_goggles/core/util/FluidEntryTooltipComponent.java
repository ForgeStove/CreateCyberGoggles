package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.fluids.FluidStack;
public record FluidEntryTooltipComponent(FluidStack fluid, int capacityMb, int indent) implements TooltipComponent {}
