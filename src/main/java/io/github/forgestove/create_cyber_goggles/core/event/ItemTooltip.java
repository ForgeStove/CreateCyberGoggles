package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class ItemTooltip {
	public static void tick(@NotNull ItemTooltipEvent event) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		var stack = event.getItemStack();
		var tooltip = event.getToolTip();
		backtank(stack, tooltip);
		divingBoot(stack, tooltip);
	}
	private static void backtank(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!(stack.getItem() instanceof BacktankItem)) return;
		var component = CreateLang.translate("gui.goggles.fluid_container.capacity")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.fraction((int) BacktankUtil.getAir(stack), BacktankUtil.maxAir(stack)))
			.component();
		tooltip.add(1, component);
	}
	private static void divingBoot(ItemStack stack, List<Component> tooltip) {
		if (!(stack.getItem() instanceof DivingBootsItem)) return;
		var component = CCGLang.translate(CCG.CONFIG.misc.allowDivingBoot ? "message.enabled" : "message.disabled").component();
		tooltip.add(1, component);
	}
}
