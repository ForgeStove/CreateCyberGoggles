package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
@Mixin(BasinBlockEntity.class)
public abstract class BasinBlockEntityMixin implements IHaveGoggleInformation, Self<BasinBlockEntity> {
	@Shadow public SmartFluidTankBehaviour inputTank;
	@Shadow protected SmartFluidTankBehaviour outputTank;
	@Unique
	private static @NotNull List<ItemStack> ccg$collectItems(@NotNull IItemHandler inventory) {
		var items = new ArrayList<ItemStack>();
		for (var slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (stack.isEmpty()) continue;
			items.add(stack.copy());
		}
		return items;
	}
	@Unique
	private static @NotNull List<FluidStack> ccg$collectFluids(SmartFluidTankBehaviour tankBehaviour, List<Integer> capacities) {
		var fluids = new ArrayList<FluidStack>();
		if (tankBehaviour == null) return fluids;
		IFluidHandler handler = tankBehaviour.getCapability();
		if (handler == null) return fluids;
		for (var tank = 0; tank < handler.getTanks(); tank++) {
			FluidStack fluid = handler.getFluidInTank(tank);
			if (fluid.isEmpty()) continue;
			fluids.add(fluid.copy());
			capacities.add(handler.getTankCapacity(tank));
		}
		return fluids;
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		BasinBlockEntity basin = thiz();
		List<ItemStack> inputItems = ccg$collectItems(basin.getInputInventory());
		List<ItemStack> outputItems = ccg$collectItems(basin.getOutputInventory());
		var inputCapacities = new ArrayList<Integer>();
		var outputCapacities = new ArrayList<Integer>();
		List<FluidStack> inputFluids = ccg$collectFluids(inputTank, inputCapacities);
		List<FluidStack> outputFluids = ccg$collectFluids(outputTank, outputCapacities);
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
