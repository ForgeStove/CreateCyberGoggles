package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidListTooltipComponent.FluidListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemEntryTooltipComponent.ItemEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemListTooltipComponent.ItemListTooltipComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.*;

import java.util.List;
@SuppressWarnings("unused")
public class CCGLang {
	@Deprecated(forRemoval = true)
	public static @NotNull CCGLangBuilder translate(String langKey) {
		return builder().translate(langKey);
	}
	@Contract(value = " -> new", pure = true)
	public static @NotNull CCGLangBuilder builder() {
		return new CCGLangBuilder(CCG.ID);
	}
	public static @NotNull CCGLangBuilder add(Component component) {
		return builder().add(component);
	}
	public static @NotNull CCGLangBuilder add(CCGLangBuilder builder) {
		return builder().add(builder);
	}
	public static @NotNull CCGLangBuilder translate(String langKey, ChatFormatting format) {
		return builder().translate(langKey, format);
	}
	public static @NotNull CCGLangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}
	public static @NotNull CCGLangBuilder translate(String langKey, ChatFormatting format, Object... args) {
		return builder().translate(langKey, format, args);
	}
	public static @NotNull CCGLangBuilder text(String text) {
		return builder().text(text);
	}
	public static @NotNull CCGLangBuilder text(String literalText, ChatFormatting format) {
		return builder().text(literalText, format);
	}
	public static @NotNull CCGLangBuilder enabled(boolean enabled) {
		return builder().enabled(enabled);
	}
	public static @NotNull CCGLangBuilder number(int number) {
		return builder().number(number);
	}
	public static @NotNull CCGLangBuilder number(int number, int color) {
		return builder().number(number, color);
	}
	public static @NotNull CCGLangBuilder number(int number, ChatFormatting format) {
		return builder().number(number, format);
	}
	public static @NotNull CCGLangBuilder number(float number, int color) {
		return builder().number(number, color);
	}
	public static @NotNull CCGLangBuilder number(float number, ChatFormatting format) {
		return builder().number(number, format);
	}
	public static @NotNull CCGLangBuilder progress(float progress, int totalBars) {
		return builder().progress(progress, totalBars);
	}
	public static @NotNull CCGLangBuilder number(double number) {
		return builder().number(number);
	}
	public static @NotNull CCGLangBuilder number(double number, int color) {
		return builder().number(number, color);
	}
	public static @NotNull CCGLangBuilder number(double number, ChatFormatting format) {
		return builder().number(number, format);
	}
	public static @NotNull CCGLangBuilder number(float number) {
		return builder().number(number);
	}
	public static @NotNull CCGLangBuilder fraction(int current, int total) {
		return builder().fraction(current, total);
	}
	public static @NotNull CCGLangBuilder fraction(float current, float total) {
		return builder().fraction(current, total);
	}
	public static @NotNull CCGLangBuilder seconds() {
		return builder().add(CreateLang.translate("generic.unit.seconds").component());
	}
	public static @NotNull CCGLangBuilder itemEntry(@NotNull ItemStack stack) {
		return itemEntry(stack, item(stack).component().copy());
	}
	public static @NotNull CCGLangBuilder itemEntry(@NotNull ItemStack stack, @NotNull Component label) {
		var marker = Component.empty();
		TooltipComponentUtil.ITEM_ENTRY_MAP.put(marker, new ItemEntryTooltipComponent(stack.copy(), 0, label.copy()));
		return builder().add(marker);
	}
	public static @NotNull CCGLangBuilder item(@NotNull ItemStack stack) {
		return builder().add(stack.getHoverName().copy().setStyle(stack.getDisplayName().getStyle()));
	}
	public static @NotNull CCGLangBuilder itemList(@NotNull List<ItemStack> items, int maxColumns) {
		var marker = Component.empty();
		var copied = items.stream().map(ItemStack::copy).toList();
		TooltipComponentUtil.ITEM_LIST_MAP.put(marker, new ItemListTooltipComponent(copied, 0, maxColumns));
		return builder().add(marker);
	}
	public static @NotNull CCGLangBuilder fluidEntry(@NotNull FluidStack fluid, int capacityMb) {
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
