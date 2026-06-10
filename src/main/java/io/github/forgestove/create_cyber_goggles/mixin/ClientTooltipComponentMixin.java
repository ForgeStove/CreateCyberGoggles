package io.github.forgestove.create_cyber_goggles.mixin;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidListTooltipComponent.FluidListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemEntryTooltipComponent.ItemEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemListTooltipComponent.ItemListTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
	@Inject(
		method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)"
			+ "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At("HEAD"), cancellable = true
	)
	private static void ccg$create(TooltipComponent component, CallbackInfoReturnable<ClientTooltipComponent> cir) {
		if (component instanceof FluidEntryTooltipComponent(
			var fluid, var indent3, var capacityMb
		)) cir.setReturnValue(new ClientFluidEntryTooltipComponent(fluid, indent3, capacityMb, 0));
		else if (component instanceof FluidListTooltipComponent(
			var fluids, var indent2, var columns
		)) cir.setReturnValue(new ClientFluidListTooltipComponent(fluids, indent2, columns));
		else if (component instanceof ItemEntryTooltipComponent(
			var stack, var indent1, var label
		)) cir.setReturnValue(new ClientItemEntryTooltipComponent(stack, indent1, label));
		else if (component instanceof ItemListTooltipComponent(
			var items, var indent, var maxColumns
		)) cir.setReturnValue(new ClientItemListTooltipComponent(items, indent, maxColumns));
	}
}
