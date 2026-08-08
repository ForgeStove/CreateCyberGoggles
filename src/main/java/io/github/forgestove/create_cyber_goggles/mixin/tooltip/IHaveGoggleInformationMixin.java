package io.github.forgestove.create_cyber_goggles.mixin.tooltip;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.thirst.WaterPurityHelper;
import io.github.forgestove.create_cyber_goggles.core.factory.CCGMods;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
@Mixin(IHaveGoggleInformation.class)
public interface IHaveGoggleInformationMixin {
	@WrapMethod(method = "containedFluidTooltip")
	private boolean containedFluidTooltip(
		List<Component> tooltip,
		boolean isPlayerSneaking,
		IFluidHandler handler,
		Operation<Boolean> original
	) {
		if (!CCG.config.tooltip.fluidContainer) return original.call(tooltip, isPlayerSneaking, handler);
		if (handler == null || handler.getTanks() == 0) return original.call(tooltip, isPlayerSneaking, handler);
		CCGLang.add(Component.translatable("create.gui.goggles.fluid_container")).forGoggles(tooltip);
		var isEmpty = true;
		for (var i = 0; i < handler.getTanks(); i++) {
			var fluidStack = handler.getFluidInTank(i);
			if (fluidStack.isEmpty()) continue;
			var purify = CCGMods.thirst.runIfInstalled(() -> WaterPurityHelper.getWaterPurity(fluidStack)).orElse(null);
			if (purify != null) CCGLang.fluidEntry(fluidStack, handler.getTankCapacity(i), purify.component()).forGoggles(tooltip);
			else CCGLang.fluidEntry(fluidStack, handler.getTankCapacity(i)).forGoggles(tooltip);
			isEmpty = false;
		}
		if (handler.getTanks() > 1) {
			if (isEmpty && !tooltip.isEmpty()) tooltip.removeLast();
			return true;
		}
		if (isEmpty) for (var i = 0; i < handler.getTanks(); i++)
			CCGLang.fluidEntry(FluidStack.EMPTY, handler.getTankCapacity(i)).forGoggles(tooltip);
		return true;
	}
}
