package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
@Mixin(value = BasinBlockEntity.class, remap = false)
public abstract class BasinBlockEntityMixin implements IHaveGoggleInformation, Self<BasinBlockEntity> {
	@Shadow public SmartFluidTankBehaviour inputTank;
	@Shadow protected SmartFluidTankBehaviour outputTank;
	@Unique
	private static @NotNull List<ItemStack> ccg$collectItems(@NotNull IItemHandler inventory) {
		var items = new ArrayList<ItemStack>();
		for (var slot = 0; slot < inventory.getSlots(); slot++) {
			var stack = inventory.getStackInSlot(slot);
			if (stack.isEmpty()) continue;
			items.add(stack.copy());
		}
		return items;
	}
	@Unique
	private static @NotNull List<FluidStack> ccg$collectFluids(SmartFluidTankBehaviour tankBehaviour, List<Integer> capacities) {
		var fluids = new ArrayList<FluidStack>();
		if (tankBehaviour == null) return fluids;
		var optional = tankBehaviour.getCapability();
		var resolve = optional.resolve();
		if (resolve.isEmpty()) return fluids;
		var handler = resolve.get();
		for (var tank = 0; tank < handler.getTanks(); tank++) {
			var fluid = handler.getFluidInTank(tank);
			if (fluid.isEmpty()) continue;
			fluids.add(fluid.copy());
			capacities.add(handler.getTankCapacity(tank));
		}
		return fluids;
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		var basin = self();
		var inputItems = ccg$collectItems(basin.getInputInventory());
		var outputItems = ccg$collectItems(basin.getOutputInventory());
		var inputCapacities = new ArrayList<Integer>();
		var outputCapacities = new ArrayList<Integer>();
		var inputFluids = ccg$collectFluids(inputTank, inputCapacities);
		var outputFluids = ccg$collectFluids(outputTank, outputCapacities);
		cir.setReturnValue(GoggleTooltipUtil.basin(
			tooltip,
			inputItems,
			outputItems,
			inputFluids,
			outputFluids,
			inputCapacities,
			outputCapacities
		));
	}
}
