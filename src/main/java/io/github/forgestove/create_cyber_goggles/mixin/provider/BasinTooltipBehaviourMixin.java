package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.BasinBlockEntityAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
@Mixin(BasinTooltipBehaviour.class)
public abstract class BasinTooltipBehaviourMixin extends TooltipBehaviour<BasinBlockEntity>
	implements IHaveGoggleInformation, Self<BasinBlockEntity> {
	public BasinTooltipBehaviourMixin(BasinBlockEntity be) {
		super(be);
	}
	@Unique
	private static @NotNull List<FluidStack> ccg$collectFluids(SmartFluidTankBehaviour tankBehaviour, List<Integer> capacities) {
		var fluids = new ArrayList<FluidStack>();
		if (tankBehaviour == null) return fluids;
		var handler = tankBehaviour.getCapability();
		if (handler == null) return fluids;
		for (var i = 0; i < handler.size(); i++) {
			var fluid = handler.getStack(i);
			if (fluid.isEmpty()) continue;
			fluids.add(fluid.copy());
			capacities.add(handler.getMaxAmount(fluid));
		}
		return fluids;
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		var basin = thiz();
		var inputItems = new ArrayList<ItemStack>();
		var outputItems = new ArrayList<ItemStack>();
		if (basin.itemCapability != null) for (var i = 0; i < basin.itemCapability.getContainerSize(); i++) {
			var stack = basin.itemCapability.getItem(i);
			if (stack.isEmpty()) continue;
			if (i < 9) inputItems.add(stack);
			else outputItems.add(stack);
		}
		var inputCapacities = new ArrayList<Integer>();
		var outputCapacities = new ArrayList<Integer>();
		var inputFluids = ccg$collectFluids(basin.inputTank, inputCapacities);
		var outputFluids = ccg$collectFluids(((BasinBlockEntityAccessor) basin).getOutputTank(), outputCapacities);
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
