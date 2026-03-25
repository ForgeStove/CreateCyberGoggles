package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidListTooltipComponent.FluidListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemEntryTooltipComponent.ItemEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemListTooltipComponent.ItemListTooltipComponent;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.*;

import java.awt.Color;
import java.util.List;

import static net.minecraft.ChatFormatting.*;
public class CCGLang {
	@Contract(value = " -> new", pure = true)
	public static @NotNull CCGLangBuilder builder() {
		return new CCGLangBuilder(CCG.ID);
	}
	public static @NotNull CCGLangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}
	public static @NotNull CCGLangBuilder translate(ChatFormatting format, String langKey, Object... args) {
		return builder().translate(format, langKey, args);
	}
	public static @NotNull CCGLangBuilder text(String text) {
		return builder().text(text);
	}
	public static @NotNull CCGLangBuilder text(ChatFormatting format, String literalText) {
		return builder().text(format, literalText);
	}
	public static @NotNull CCGLangBuilder number(float number) {
		return text(LangNumberFormat.format(Double.parseDouble(Float.toString(number))));
	}
	public static @NotNull CCGLangBuilder number(int number) {
		return text(String.valueOf(number));
	}
	public static @NotNull CCGLangBuilder number(ChatFormatting format, int number) {
		return text(format, String.valueOf(number));
	}
	public static @NotNull CCGLangBuilder progress(float progress, int totalBars) {
		var filledBars = (int) (Mth.clamp(progress, 0, 1) * totalBars);
		return text(GREEN, "|".repeat(filledBars)).text(GRAY, "|".repeat(totalBars - filledBars));
	}
	public static @NotNull CCGLangBuilder fraction(int current, int total) {
		return number(current).color(Color.HSBtoRGB((float) current / total * 0.33F, 1, 1))
			.text(GRAY, " / ")
			.add(number(total).style(DARK_GRAY));
	}
	public static @NotNull CCGLangBuilder fraction(float current, float total) {
		return number(current).color(Color.HSBtoRGB(current / total * 0.33F, 1, 1)).text(GRAY, " / ").add(number(total).style(DARK_GRAY));
	}
	public static @NotNull CCGLangBuilder enabled(boolean enabled) {
		return enabled ? translate(GREEN, "message.enabled") : translate(RED, "message.disabled");
	}
	public static @NotNull CCGLangBuilder seconds() {
		return builder().add(CreateLang.translate("generic.unit.seconds").component());
	}
	public static @NotNull CCGLangBuilder itemWithoutCount(@NotNull ItemStack stack) {
		return builder().add(stack.getHoverName().copy().setStyle(stack.getDisplayName().getStyle()));
	}
	public static @NotNull CCGLangBuilder item(@NotNull ItemStack stack) {
		return itemWithoutCount(stack).text(GRAY, " x%d".formatted(stack.getCount()));
	}
	public static @NotNull CCGLangBuilder itemList(@NotNull List<ItemStack> items, int maxColumns) {
		var marker = Component.empty();
		var copied = items.stream().map(ItemStack::copy).toList();
		TooltipComponentUtil.ITEM_LIST_MAP.put(marker, new ItemListTooltipComponent(copied, 0, maxColumns));
		return builder().add(marker);
	}
	public static @NotNull CCGLangBuilder item(@NotNull ItemStack stack, @NotNull Component label) {
		var marker = Component.empty();
		TooltipComponentUtil.ITEM_ENTRY_MAP.put(marker, new ItemEntryTooltipComponent(stack.copy(), 0, label.copy()));
		return builder().add(marker);
	}
	public static @NotNull CCGLangBuilder fluid(@NotNull FluidStack fluid, int capacityMb) {
		var marker = Component.empty();
		TooltipComponentUtil.FLUID_ENTRY_MAP.put(marker, new FluidEntryTooltipComponent(fluid.copy(), 0, Math.max(1, capacityMb)));
		return builder().add(marker);
	}
	@SuppressWarnings("unused")
	public static @NotNull CCGLangBuilder fluidList(@NotNull List<FluidStack> fluids, int maxColumns) {
		var marker = Component.empty();
		var copied = fluids.stream().map(FluidStack::copy).toList();
		TooltipComponentUtil.FLUID_LIST_MAP.put(marker, new FluidListTooltipComponent(copied, 0, maxColumns));
		return builder().add(marker);
	}
}
