package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.*;

import java.awt.Color;
import java.util.*;

import static net.minecraft.ChatFormatting.*;
public class CCGLang {
	private static final IdentityHashMap<Component, ItemListTooltipComponent> ITEM_LIST_MAP = new IdentityHashMap<>();
	private static final IdentityHashMap<Component, ItemEntryTooltipComponent> ITEM_ENTRY_MAP = new IdentityHashMap<>();
	private static final IdentityHashMap<Component, FluidListTooltipComponent> FLUID_LIST_MAP = new IdentityHashMap<>();
	private static final IdentityHashMap<Component, FluidEntryTooltipComponent> FLUID_ENTRY_MAP = new IdentityHashMap<>();
	private static @NotNull MutableComponent marker() {
		return Component.empty();
	}
	private static int spacesOnlyCount(@NotNull Component component) {
		var text = component.getString();
		if (text.isEmpty()) return 0;
		for (var i = 0; i < text.length(); i++)
			if (text.charAt(i) != ' ') return 0;
		return text.length();
	}
	private static <T> @Nullable MarkerResult<T> removeMarker(
		@NotNull Component component,
		@NotNull IdentityHashMap<Component, T> map,
		int indent
	) {
		var data = map.remove(component);
		if (data != null) return new MarkerResult<>(data, indent);
		var runningIndent = indent + spacesOnlyCount(component);
		for (var sibling : component.getSiblings()) {
			var result = removeMarker(sibling, map, runningIndent);
			if (result != null) return result;
			runningIndent += spacesOnlyCount(sibling);
		}
		return null;
	}
	private static <T> boolean hasMarker(@NotNull Component component, @NotNull IdentityHashMap<Component, T> map) {
		if (map.containsKey(component)) return true;
		for (var sibling : component.getSiblings())
			if (hasMarker(sibling, map)) return true;
		return false;
	}
	public static @Nullable ItemListTooltipComponent removeItemList(@NotNull Object key) {
		if (!(key instanceof Component comp)) return null;
		var result = removeMarker(comp, ITEM_LIST_MAP, 0);
		if (result == null) return null;
		var data = result.data();
		return new ItemListTooltipComponent(data.items(), data.maxColumns(), result.indent());
	}
	public static @Nullable ItemEntryTooltipComponent removeItemEntry(@NotNull Object key) {
		if (!(key instanceof Component comp)) return null;
		var result = removeMarker(comp, ITEM_ENTRY_MAP, 0);
		if (result == null) return null;
		var data = result.data();
		return new ItemEntryTooltipComponent(data.stack(), data.label(), result.indent());
	}
	public static @Nullable FluidEntryTooltipComponent removeFluidEntry(@NotNull Object key) {
		if (!(key instanceof Component comp)) return null;
		var result = removeMarker(comp, FLUID_ENTRY_MAP, 0);
		if (result == null) return null;
		var data = result.data();
		return new FluidEntryTooltipComponent(data.fluid(), data.capacityMb(), result.indent());
	}
	public static @Nullable FluidListTooltipComponent removeFluidList(@NotNull Object key) {
		if (!(key instanceof Component comp)) return null;
		var result = removeMarker(comp, FLUID_LIST_MAP, 0);
		if (result == null) return null;
		var data = result.data();
		return new FluidListTooltipComponent(data.fluids(), data.maxColumns(), result.indent());
	}
	public static boolean hasItemList(@NotNull Object key) {
		return key instanceof Component comp && hasMarker(comp, ITEM_LIST_MAP);
	}
	public static boolean hasItemEntry(@NotNull Object key) {
		return key instanceof Component comp && hasMarker(comp, ITEM_ENTRY_MAP);
	}
	public static boolean hasFluidEntry(@NotNull Object key) {
		return key instanceof Component comp && hasMarker(comp, FLUID_ENTRY_MAP);
	}
	public static boolean hasFluidList(@NotNull Object key) {
		return key instanceof Component comp && hasMarker(comp, FLUID_LIST_MAP);
	}
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
	public static @NotNull CCGLangBuilder number(double number) {
		return text(LangNumberFormat.format(number));
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
		var marker = marker();
		var copied = items.stream().map(ItemStack::copy).toList();
		ITEM_LIST_MAP.put(marker, new ItemListTooltipComponent(copied, maxColumns, 0));
		return builder().add(marker);
	}
	public static @NotNull CCGLangBuilder item(@NotNull ItemStack stack, @NotNull Component label) {
		var marker = marker();
		ITEM_ENTRY_MAP.put(marker, new ItemEntryTooltipComponent(stack.copy(), label.copy(), 0));
		return builder().add(marker);
	}
	public static @NotNull CCGLangBuilder fluid(@NotNull FluidStack fluid, int capacityMb) {
		var marker = marker();
		FLUID_ENTRY_MAP.put(marker, new FluidEntryTooltipComponent(fluid.copy(), Math.max(1, capacityMb), 0));
		return builder().add(marker);
	}
	@SuppressWarnings("unused")
	public static @NotNull CCGLangBuilder fluidList(@NotNull List<FluidStack> fluids, int maxColumns) {
		var marker = marker();
		var copied = fluids.stream().map(FluidStack::copy).toList();
		FLUID_LIST_MAP.put(marker, new FluidListTooltipComponent(copied, maxColumns, 0));
		return builder().add(marker);
	}
	private record MarkerResult<T>(T data, int indent) {}
}
