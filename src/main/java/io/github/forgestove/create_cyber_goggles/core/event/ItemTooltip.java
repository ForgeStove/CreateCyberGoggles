package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.datafixers.util.Either;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockItem;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.event.RenderTooltipEvent.GatherComponents;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public class ItemTooltip {
	public static void itemTooltip(@NotNull ItemTooltipEvent event) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		var stack = event.getItemStack();
		var tooltip = event.getToolTip();
		goggles(stack, tooltip);
		backtank(stack, tooltip);
		divingBoots(stack, tooltip);
		wrench(stack, tooltip);
		linkedController(stack, tooltip);
		redstoneRequester(stack, tooltip);
		toolbox(stack, tooltip);
		enderChest(stack, tooltip);
		container(stack, tooltip);
		fluidContainer(stack, tooltip);
	}
	public static void gatherComponents(@NotNull GatherComponents event) {
		var elements = event.getTooltipElements();
		for (var i = 0; i < elements.size(); i++) {
			var left = elements.get(i).left().orElse(null);
			if (!(left instanceof Component comp)) continue;
			var entry = TooltipComponentUtil.removeItemEntry(comp);
			if (entry != null) {
				elements.set(i, Either.right(entry));
				continue;
			}
			var fluid = TooltipComponentUtil.removeFluidEntry(comp);
			if (fluid != null) {
				elements.set(i, Either.right(fluid));
				continue;
			}
			var fluidList = TooltipComponentUtil.removeFluidList(comp);
			if (fluidList != null) {
				elements.set(i, Either.right(fluidList));
				continue;
			}
			var data = TooltipComponentUtil.removeItemList(comp);
			if (data != null) elements.set(i, Either.right(data));
		}
	}
	private static void goggles(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.goggles) return;
		if (!(stack.getItem() instanceof GogglesItem)) return;
		var component = CCGLang.enabled(GogglesItem.isWearingGoggles(mc.player)).component();
		tooltip.add(1, component);
	}
	private static void backtank(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.backtank) return;
		if (!(stack.getItem() instanceof BacktankItem)) return;
		var component = CreateLang.translate("gui.goggles.fluid_container.capacity")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.fraction(BacktankUtil.getAir(stack), BacktankUtil.maxAir(stack)).component())
			.component();
		tooltip.add(1, component);
	}
	private static void divingBoots(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.divingBoots) return;
		if (!(stack.getItem() instanceof DivingBootsItem)) return;
		var component = CCGLang.enabled(CCG.config.misc.allowDivingBoot).component();
		tooltip.add(1, component);
	}
	private static void wrench(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.wrench) return;
		if (!(stack.getItem() instanceof WrenchItem)) return;
		var component = CCGLang.builder()
			.translate("config.option.wrench.leftClickFastDismantle")
			.space()
			.add(CCGLang.enabled(CCG.config.wrench.leftClickFastDismantle))
			.component();
		tooltip.add(1, component);
	}
	private static void linkedController(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.linkedController) return;
		if (!(stack.getItem() instanceof LinkedControllerItem)) return;
		var frequencyItems = LinkedControllerItem.getFrequencyItems(stack);
		var items = new ArrayList<ItemStack>(12);
		var hasAnyItem = false;
		for (var row = 0; row < 2; row++)
			for (var column = 0; column < 6; column++) {
				var slotIndex = column * 2 + row;
				var slot = frequencyItems.getStackInSlot(slotIndex);
				if (!slot.isEmpty()) hasAnyItem = true;
				items.add(slot.copyWithCount(1));
			}
		if (!hasAnyItem) return;
		CCGLang.itemList(items, 6).addTo(1, tooltip);
	}
	private static void redstoneRequester(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.redstoneRequester) return;
		if (!(stack.getItem() instanceof RedstoneRequesterBlockItem)) return;
		var beData = stack.getComponents().get(DataComponents.BLOCK_ENTITY_DATA);
		if (beData == null || !beData.contains("EncodedRequest")) return;
		if (mc.level == null) return;
		var encodedRequestTag = beData.copyTag().getCompound("EncodedRequest");
		var encodedRequest = CatnipCodecUtils.decode(PackageOrderWithCrafts.CODEC, mc.level.registryAccess(), encodedRequestTag)
			.orElse(PackageOrderWithCrafts.empty());
		if (encodedRequest.isEmpty()) return;
		var items = new ArrayList<ItemStack>();
		encodedRequest.stacks().forEach(bigStack -> items.add(bigStack.stack.copyWithCount(bigStack.count)));
		if (items.isEmpty()) return;
		CCGLang.itemList(items, 9).addTo(2, tooltip);
	}
	private static void toolbox(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.toolbox) return;
		if (!AllItemTags.TOOLBOXES.matches(stack)) return;
		var inventory = stack.getComponents().get(AllDataComponents.TOOLBOX_INVENTORY);
		if (inventory == null) return;
		var compartments = 8;
		var stacksPerCompartment = ToolboxInventory.STACKS_PER_COMPARTMENT;
		List<ItemStack> items = new ArrayList<>();
		for (var compartment = 0; compartment < compartments; compartment++) {
			var baseIndex = compartment * stacksPerCompartment;
			var consolidated = ItemStack.EMPTY;
			var totalCount = 0;
			for (var offset = 0; offset < stacksPerCompartment; offset++) {
				var slotIndex = baseIndex + offset;
				if (slotIndex >= inventory.getSlots()) break;
				var slot = inventory.getStackInSlot(slotIndex);
				if (slot.isEmpty()) continue;
				if (consolidated.isEmpty()) {
					consolidated = slot.copyWithCount(1);
					totalCount = slot.getCount();
				} else if (ItemStack.isSameItemSameComponents(consolidated, slot)) totalCount += slot.getCount();
			}
			items.add(consolidated.copyWithCount(totalCount));
		}
		if (!items.isEmpty()) CCGLang.itemList(items, 4).addTo(1, tooltip);
	}
	private static void container(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.container) return;
		var container = stack.getComponents().get(DataComponents.CONTAINER);
		if (container == null) return;
		var items = new ArrayList<ItemStack>();
		for (var i = 0; i < container.getSlots(); i++) items.add(container.getStackInSlot(i));
		if (items.isEmpty()) return;
		var advanced = mc.options.advancedItemTooltips ? 2 : 0;
		if (tooltip.size() > 1) tooltip.subList(1, tooltip.size() - advanced).clear();
		CCGLang.itemList(items, 9).addTo(1, tooltip);
	}
	private static void fluidContainer(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.container) return;
		var handler = FluidUtil.getFluidHandler(stack).orElse(null);
		if (handler == null || handler.getTanks() == 0) return;
		var entries = new ArrayList<FluidStack>();
		var capacities = new ArrayList<Integer>();
		for (var i = 0; i < handler.getTanks(); i++) {
			var fluid = handler.getFluidInTank(i);
			if (fluid.isEmpty()) continue;
			entries.add(fluid.copy());
			capacities.add(handler.getTankCapacity(i));
		}
		if (entries.isEmpty()) {
			entries.add(FluidStack.EMPTY);
			capacities.add(handler.getTankCapacity(0));
		}
		var advanced = mc.options.advancedItemTooltips ? 2 : 0;
		if (tooltip.size() > 1) tooltip.subList(1, tooltip.size() - advanced).clear();
		for (var i = 0; i < entries.size(); i++) {
			var fluid = entries.get(i);
			var capacity = i < capacities.size() ? capacities.get(i) : Math.max(1, fluid.getAmount());
			CCGLang.fluid(fluid, capacity).addTo(1, tooltip);
		}
	}
	private static void enderChest(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.container) return;
		if (!stack.is(Items.ENDER_CHEST)) return;
		EnderChestTooltipUtil.appendForItem(tooltip);
	}
}
