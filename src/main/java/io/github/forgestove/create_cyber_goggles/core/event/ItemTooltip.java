package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.foundation.utility.Lang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public class ItemTooltip {
	public static void itemTooltip(@NotNull ItemTooltipEvent event) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		var stack = event.getItemStack();
		var tooltip = event.getToolTip();
		goggles(stack, tooltip);
		backtank(stack, tooltip);
		divingBoots(stack, tooltip);
		wrench(stack, tooltip);
		toolbox(stack, tooltip);
	}
	private static void goggles(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!(stack.getItem() instanceof GogglesItem)) return;
		var component = CCGLang.enabled(GogglesItem.isWearingGoggles(mc.player)).component();
		tooltip.add(1, component);
	}
	private static void backtank(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!(stack.getItem() instanceof BacktankItem)) return;
		var component = Lang.translate("gui.goggles.fluid_container.capacity")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.fraction((int) BacktankUtil.getAir(stack), BacktankUtil.maxAir(stack)))
			.component();
		tooltip.add(1, component);
	}
	private static void divingBoots(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!(stack.getItem() instanceof DivingBootsItem)) return;
		var component = CCGLang.enabled(CCG.CONFIG.misc.allowDivingBoot).component();
		tooltip.add(1, component);
	}
	private static void wrench(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!(stack.getItem() instanceof WrenchItem)) return;
		var component = CCGLang.configBuilder()
			.translate("option.wrench.leftClickFastDismantle")
			.space()
			.add(CCGLang.enabled(CCG.CONFIG.wrench.leftClickFastDismantle))
			.component();
		tooltip.add(1, component);
	}
	private static void toolbox(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!AllItemTags.TOOLBOXES.matches(stack)) return;
		var tag = stack.getOrCreateTag();
		if (!tag.contains("Inventory")) return;
		List<Component> list = new ArrayList<>();
		var inventory = tag.getCompound("Inventory");
		if (!inventory.contains("Items")) return;
		var items = inventory.getList("Items", Tag.TAG_COMPOUND);
		for (var i = 0; i < items.size(); i++) {
			var slotTag = items.getCompound(i);
			var slotStack = ItemStack.of(slotTag);
			if (slotStack.isEmpty()) continue;
			CCGLang.item(slotStack).addTo(list);
		}
		tooltip.addAll(1, list);
	}
}
