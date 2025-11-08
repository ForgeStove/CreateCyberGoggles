package io.github.forgestove.create_cyber_goggles.core.event;
import com.zurrtum.create.*;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.armor.*;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.content.equipment.wrench.WrenchItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public class ItemTooltip {
	public static void itemTooltip(ItemStack stack, TooltipContext ignoredContext, TooltipFlag ignoredFlag, List<Component> tooltip) {
		if (!CCG.CONFIG.goggles.extraItemTooltip) return;
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
		var component = CreateLang.translate("gui.goggles.fluid_container.capacity")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.fraction(BacktankUtil.getAir(stack), BacktankUtil.maxAir(stack)))
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
		if (!stack.is(AllItemTags.TOOLBOXES)) return;
		var inventory = stack.getComponents().get(AllDataComponents.TOOLBOX_INVENTORY);
		if (inventory == null) return;
		List<Component> list = new ArrayList<>();
		for (var i = 0; i < inventory.getContainerSize(); i++) {
			var slot = inventory.getItem(i);
			if (slot.isEmpty()) continue;
			CCGLang.item(slot).addTo(list);
		}
		tooltip.addAll(1, list);
	}
}
