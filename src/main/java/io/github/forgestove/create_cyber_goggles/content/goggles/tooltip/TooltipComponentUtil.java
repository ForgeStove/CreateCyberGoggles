package io.github.forgestove.create_cyber_goggles.content.goggles.tooltip;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientFluidListTooltipComponent.FluidListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientItemEntryTooltipComponent.ItemEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientItemListTooltipComponent.ItemListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientSchematicPreviewTooltipComponent.SchematicPreviewTooltip;
import net.minecraft.network.chat.*;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.*;

import java.util.*;
public class TooltipComponentUtil {
	public static final IdentityHashMap<Component, ItemListTooltipComponent> ITEM_LIST_MAP = new IdentityHashMap<>();
	public static final IdentityHashMap<Component, ItemEntryTooltipComponent> ITEM_ENTRY_MAP = new IdentityHashMap<>();
	public static final IdentityHashMap<Component, FluidListTooltipComponent> FLUID_LIST_MAP = new IdentityHashMap<>();
	public static final IdentityHashMap<Component, FluidEntryTooltipComponent> FLUID_ENTRY_MAP = new IdentityHashMap<>();
	public static final IdentityHashMap<Component, SchematicPreviewTooltip> SCHEMATIC_PREVIEW_MAP = new IdentityHashMap<>();
	private static int spacesOnlyCount(@NotNull Component component) {
		String text = component.getString();
		if (text.isEmpty()) return 0;
		for (var i = 0; i < text.length(); i++)
			if (text.charAt(i) != ' ') return 0;
		return text.length();
	}
	private static <T extends TooltipComponent> @Nullable MarkerResult<T> removeMarker(
		@NotNull Component component,
		@NotNull IdentityHashMap<Component, T> map
	) {
		return findMarker(component, map, 0, true);
	}
	private static <T extends TooltipComponent> @Nullable MarkerResult<T> findMarker(
		@NotNull Component component,
		@NotNull IdentityHashMap<Component, T> map,
		int indent,
		boolean consume
	) {
		T data = consume ? map.remove(component) : map.get(component);
		if (data != null) return new MarkerResult<>(data, indent, component);
		int runningIndent = indent + spacesOnlyCount(component);
		for (Component sibling : component.getSiblings()) {
			MarkerResult<T> result = findMarker(sibling, map, runningIndent, consume);
			if (result != null) return result;
			runningIndent += spacesOnlyCount(sibling);
		}
		return null;
	}
	public static <T> boolean hasMarker(@NotNull Component component, @NotNull IdentityHashMap<Component, T> map) {
		if (map.containsKey(component)) return true;
		for (Component sibling : component.getSiblings())
			if (hasMarker(sibling, map)) return true;
		return false;
	}
	public static @Nullable FluidEntryTooltipComponent peekFluidEntry(@NotNull Component comp) {
		MarkerResult<FluidEntryTooltipComponent> result = findMarker(comp, FLUID_ENTRY_MAP, 0, false);
		if (result == null) return null;
		FluidEntryTooltipComponent data = result.data();
		return new FluidEntryTooltipComponent(data.fluid(), result.indent(), data.capacityMb(), 0, data.label());
	}
	public static boolean hasIcon(@NotNull Object key) {
		if (!(key instanceof Component comp)) return false;
		if (hasMarker(comp, ITEM_LIST_MAP)) return true;
		if (hasMarker(comp, ITEM_ENTRY_MAP)) return true;
		if (hasMarker(comp, FLUID_ENTRY_MAP)) return true;
		return hasMarker(comp, FLUID_LIST_MAP);
	}
	public static @Nullable MarkerSplit consumeMarker(@NotNull Component line) {
		MarkerResult<SchematicPreviewTooltip> preview = removeMarker(line, SCHEMATIC_PREVIEW_MAP);
		if (preview != null) return buildSplit(line, preview.data(), preview.marker());
		MarkerResult<ItemEntryTooltipComponent> item = removeMarker(line, ITEM_ENTRY_MAP);
		if (item != null)
			return buildSplit(line, new ItemEntryTooltipComponent(item.data().stack(), item.indent(), item.data().label()), item.marker());
		MarkerResult<ItemListTooltipComponent> itemList = removeMarker(line, ITEM_LIST_MAP);
		if (itemList != null) return buildSplit(
			line,
			new ItemListTooltipComponent(itemList.data().items(), itemList.indent(), itemList.data().maxColumns()),
			itemList.marker()
		);
		MarkerResult<FluidEntryTooltipComponent> fluid = removeMarker(line, FLUID_ENTRY_MAP);
		if (fluid != null) return buildSplit(
			line,
			new FluidEntryTooltipComponent(fluid.data().fluid(), fluid.indent(), fluid.data().capacityMb(), 0, fluid.data().label()),
			fluid.marker()
		);
		MarkerResult<FluidListTooltipComponent> fluidList = removeMarker(line, FLUID_LIST_MAP);
		if (fluidList != null) return buildSplit(
			line,
			new FluidListTooltipComponent(fluidList.data().fluids(), fluidList.indent(), fluidList.data().maxColumns()),
			fluidList.marker()
		);
		return null;
	}
	private static @NotNull MarkerSplit buildSplit(@NotNull Component line, @NotNull TooltipComponent data, @NotNull Component marker) {
		Component remaining = stripMarker(line, marker);
		if (remaining != null && remaining.getString().isBlank()) remaining = null;
		return new MarkerSplit(data, remaining);
	}
	private static @Nullable Component stripMarker(@NotNull Component line, @NotNull Component marker) {
		if (line == marker) return null;
		List<Component> siblings = line.getSiblings();
		if (siblings.isEmpty()) return line;
		var indexes = new ArrayList<Integer>();
		for (var i = 0; i < siblings.size(); i++)
			if (siblings.get(i) == marker) indexes.add(i);
		if (indexes.isEmpty()) return line;
		MutableComponent copied = line.copy();
		List<Component> list = copied.getSiblings();
		for (int i = indexes.size() - 1; i >= 0; i--)
			list.remove((int) indexes.get(i));
		return copied;
	}
	public record MarkerResult<T>(T data, int indent, Component marker) {}
	public record MarkerSplit(TooltipComponent data, @Nullable Component remaining) {}
}
