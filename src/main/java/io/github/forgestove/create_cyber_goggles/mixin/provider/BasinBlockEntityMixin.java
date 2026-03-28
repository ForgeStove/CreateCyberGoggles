package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
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
	private static @NotNull List<ItemStack> ccg$collectItems(@NotNull Container inventory) {
		var items = new ArrayList<ItemStack>();
		for (var slot = 0; slot < inventory.getContainerSize(); slot++) {
			var stack = inventory.getItem(slot);
			if (stack.isEmpty()) continue;
			items.add(stack.copy());
		}
		return items;
	}
	@Unique
	private static @NotNull List<FluidStack> ccg$collectFluids(SmartFluidTankBehaviour tankBehaviour, List<Long> capacities) {
		var fluids = new ArrayList<FluidStack>();
		if (tankBehaviour == null) return fluids;
		var handler = tankBehaviour.getTanks();
		if (handler == null) return fluids;
		for (var tankSegment : handler) {
			var tank = tankSegment.getTank();
			var fluid = tank.getFluid();
			if (fluid.isEmpty()) continue;
			fluids.add(fluid.copy());
			capacities.add(tank.getCapacity());
		}
		return fluids;
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		var basin = thiz();
		var inputItems = ccg$collectItems(basin.getInputInventory());
		var outputItems = ccg$collectItems(basin.getOutputInventory());
		var inputCapacities = new ArrayList<Long>();
		var outputCapacities = new ArrayList<Long>();
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
