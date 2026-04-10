package io.github.forgestove.create_cyber_goggles.core.util;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidListTooltipComponent.FluidListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemEntryTooltipComponent.ItemEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemListTooltipComponent.ItemListTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.*;

import java.util.IdentityHashMap;
public final class TooltipComponentUtil {
	public static final IdentityHashMap<Component, ItemListTooltipComponent> ITEM_LIST_MAP = new IdentityHashMap<>();
	public static final IdentityHashMap<Component, ItemEntryTooltipComponent> ITEM_ENTRY_MAP = new IdentityHashMap<>();
	public static final IdentityHashMap<Component, FluidListTooltipComponent> FLUID_LIST_MAP = new IdentityHashMap<>();
	public static final IdentityHashMap<Component, FluidEntryTooltipComponent> FLUID_ENTRY_MAP = new IdentityHashMap<>();
	private static int spacesOnlyCount(@NotNull Component component) {
		var text = component.getString();
		if (text.isEmpty()) return 0;
		for (var i = 0; i < text.length(); i++)
			if (text.charAt(i) != ' ') return 0;
		return text.length();
	}
	private static <T extends TooltipComponent> @Nullable MarkerResult<T> removeMarker(
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
	public static <T> boolean hasMarker(@NotNull Component component, @NotNull IdentityHashMap<Component, T> map) {
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
		return new ItemListTooltipComponent(data.items(), result.indent(), data.maxColumns());
	}
	public static @Nullable ItemEntryTooltipComponent removeItemEntry(@NotNull Object key) {
		if (!(key instanceof Component comp)) return null;
		var result = removeMarker(comp, ITEM_ENTRY_MAP, 0);
		if (result == null) return null;
		var data = result.data();
		return new ItemEntryTooltipComponent(data.stack(), result.indent(), data.label());
	}
	public static @Nullable FluidEntryTooltipComponent removeFluidEntry(@NotNull Object key) {
		if (!(key instanceof Component comp)) return null;
		var result = removeMarker(comp, FLUID_ENTRY_MAP, 0);
		if (result == null) return null;
		var data = result.data();
		return new FluidEntryTooltipComponent(data.fluid(), result.indent(), data.capacityMb());
	}
	public static @Nullable FluidListTooltipComponent removeFluidList(@NotNull Object key) {
		if (!(key instanceof Component comp)) return null;
		var result = removeMarker(comp, FLUID_LIST_MAP, 0);
		if (result == null) return null;
		var data = result.data();
		return new FluidListTooltipComponent(data.fluids(), result.indent(), data.maxColumns());
	}
	public static boolean hasIcon(@NotNull Object key) {
		return key instanceof Component comp && (
			hasMarker(comp, ITEM_LIST_MAP) || hasMarker(comp, ITEM_ENTRY_MAP) || hasMarker(comp, FLUID_ENTRY_MAP) || hasMarker(
				comp,
				FLUID_LIST_MAP
			)
		);
	}
	public record MarkerResult<T>(T data, int indent) {}
}
