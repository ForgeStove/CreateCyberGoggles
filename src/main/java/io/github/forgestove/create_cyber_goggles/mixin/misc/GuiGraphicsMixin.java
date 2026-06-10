package io.github.forgestove.create_cyber_goggles.mixin.misc;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.event.ItemTooltip;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.util.TooltipComponentUtil;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.GuiGraphicsAccessor;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements Self<GuiGraphics> {
	@Unique
	private static boolean hasAnyMarker(List<Component> components) {
		for (var component : components)
			if (TooltipComponentUtil.hasIcon(component)) return true;
		return false;
	}
	@Inject(
		method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD")
	)
	private void captureTooltipStack(Font font, ItemStack stack, int x, int y, CallbackInfo ci) {
		ItemTooltip.capturedStack = stack;
		ItemTooltip.capturedMouseX = x;
		ItemTooltip.capturedMouseY = y;
	}
	@Inject(
		method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;"
			+ "IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
		at = @At("TAIL")
	)
	private void renderTooltipOverlay(
		Font font,
		List<ClientTooltipComponent> components,
		int x,
		int y,
		ClientTooltipPositioner positioner,
		Identifier background,
		CallbackInfo ci
	) {
		ItemTooltip.renderTooltipOverlay(
			ItemTooltip.capturedStack,
			thiz(),
			font,
			components,
			ItemTooltip.capturedMouseX,
			ItemTooltip.capturedMouseY,
			positioner
		);
		ItemTooltip.capturedStack = ItemStack.EMPTY;
	}
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Inject(
		method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;"
			+ "Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V", at = @At("HEAD"), cancellable = true
	)
	private void handleVanillaTooltipMarkers(
		Font font,
		List<Component> components,
		Optional<TooltipComponent> tooltipImage,
		int x,
		int y,
		Identifier background,
		CallbackInfo ci
	) {
		if (!hasAnyMarker(components)) return;
		var parsed = new ArrayList<>();
		var fluidEntries = new ArrayList<FluidEntryTooltipComponent>();
		for (var component : components) {
			var item = TooltipComponentUtil.removeItemEntry(component);
			if (item != null) {
				parsed.add(new ClientItemEntryTooltipComponent(item.stack(), item.indent(), item.label()));
				continue;
			}
			var fluid = TooltipComponentUtil.removeFluidEntry(component);
			if (fluid != null) {
				parsed.add(fluid);
				fluidEntries.add(fluid);
				continue;
			}
			var itemList = TooltipComponentUtil.removeItemList(component);
			if (itemList != null) {
				parsed.add(new ClientItemListTooltipComponent(itemList.items(), itemList.indent(), itemList.maxColumns()));
				continue;
			}
			var fluidList = TooltipComponentUtil.removeFluidList(component);
			if (fluidList != null) {
				parsed.add(new ClientFluidListTooltipComponent(fluidList.fluids(), fluidList.indent(), fluidList.maxColumns()));
				continue;
			}
			parsed.add(ClientTooltipComponent.create(component.getVisualOrderText()));
		}
		var sharedFluidWidth = 0;
		for (var fluidEntry : fluidEntries) {
			var preferred = ClientFluidEntryTooltipComponent.preferredBarWidth(font, fluidEntry.fluid(), fluidEntry.capacityMb());
			if (preferred > sharedFluidWidth) sharedFluidWidth = preferred;
		}
		var clientComponents = new ArrayList<ClientTooltipComponent>();
		for (var value : parsed)
			if (value instanceof ClientTooltipComponent ctc) clientComponents.add(ctc);
			else if (value instanceof FluidEntryTooltipComponent(
				var fluid, var indent, var capacityMb
			)) clientComponents.add(new ClientFluidEntryTooltipComponent(fluid, indent, capacityMb, sharedFluidWidth));
		tooltipImage.ifPresent(tc -> clientComponents.add(ClientTooltipComponent.create(tc)));
		((GuiGraphicsAccessor) this).ccg$setTooltipForNextFrameInternal(
			font,
			clientComponents,
			x,
			y,
			DefaultTooltipPositioner.INSTANCE,
			background,
			false
		);
		ci.cancel();
	}
}
