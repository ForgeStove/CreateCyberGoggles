package io.github.forgestove.create_cyber_goggles.core.util;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidListTooltipComponent.FluidListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemEntryTooltipComponent.ItemEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemListTooltipComponent.ItemListTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.*;

import java.util.*;
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
		@NotNull IdentityHashMap<Component, T> map
	) {
		return findMarker(component, map, 0, true);
	}
	/** 与 removeMarker 相同，但可指定是否消费 map（peek 用于多遍遍历：先收集再替换） */
	private static <T extends TooltipComponent> @Nullable MarkerResult<T> findMarker(
		@NotNull Component component,
		@NotNull IdentityHashMap<Component, T> map,
		int indent,
		boolean consume
	) {
		var data = consume ? map.remove(component) : map.get(component);
		if (data != null) return new MarkerResult<>(data, indent, component);
		var runningIndent = indent + spacesOnlyCount(component);
		for (var sibling : component.getSiblings()) {
			var result = findMarker(sibling, map, runningIndent, consume);
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
	/** 只读版 removeFluidEntry：不消费 marker，用于 GatherComponents 阶段先统一条宽再替换 */
	public static @Nullable FluidEntryTooltipComponent peekFluidEntry(@NotNull Component comp) {
		var result = findMarker(comp, FLUID_ENTRY_MAP, 0, false);
		if (result == null) return null;
		var data = result.data();
		return new FluidEntryTooltipComponent(data.fluid(), result.indent(), data.capacityMb(), 0, data.label());
	}
	public static boolean hasIcon(@NotNull Object key) {
		if (!(key instanceof Component comp)) return false;
		if (hasMarker(comp, ITEM_LIST_MAP)) return true;
		if (hasMarker(comp, ITEM_ENTRY_MAP)) return true;
		if (hasMarker(comp, FLUID_ENTRY_MAP)) return true;
		return hasMarker(comp, FLUID_LIST_MAP);
	}
	/**
	 * 消费行内首个 CCGLang marker（4 种之一），返回 UI 数据与剔除 marker 后的剩余文本行。
	 * 解决 marker 与普通文本混行时整行替换导致同行文本丢失的问题：剩余文本单独成行渲染，UI 独立成行。
	 */
	public static @Nullable MarkerSplit consumeMarker(@NotNull Component line) {
		var item = removeMarker(line, ITEM_ENTRY_MAP);
		if (item != null)
			return buildSplit(line, new ItemEntryTooltipComponent(item.data().stack(), item.indent(), item.data().label()), item.marker());
		var itemList = removeMarker(line, ITEM_LIST_MAP);
		if (itemList != null) return buildSplit(
			line,
			new ItemListTooltipComponent(itemList.data().items(), itemList.indent(), itemList.data().maxColumns()),
			itemList.marker()
		);
		var fluid = removeMarker(line, FLUID_ENTRY_MAP);
		if (fluid != null) return buildSplit(
			line,
			new FluidEntryTooltipComponent(fluid.data().fluid(), fluid.indent(), fluid.data().capacityMb(), 0, fluid.data().label()),
			fluid.marker()
		);
		var fluidList = removeMarker(line, FLUID_LIST_MAP);
		if (fluidList != null) return buildSplit(
			line,
			new FluidListTooltipComponent(fluidList.data().fluids(), fluidList.indent(), fluidList.data().maxColumns()),
			fluidList.marker()
		);
		return null;
	}
	private static @NotNull MarkerSplit buildSplit(@NotNull Component line, @NotNull TooltipComponent data, @NotNull Component marker) {
		var remaining = stripMarker(line, marker);
		// marker 空不可见；剔除后只剩空白则整行即 UI，不产生空白文本行
		if (remaining != null && remaining.getString().isBlank()) remaining = null;
		return new MarkerSplit(data, remaining);
	}
	/** 从行中剔除直接 sibling 中的 marker；嵌套时原样返回（marker 为空不影响视觉）；无剩余返回 null */
	private static @Nullable Component stripMarker(@NotNull Component line, @NotNull Component marker) {
		if (line == marker) return null;
		var siblings = line.getSiblings();
		if (siblings.isEmpty()) return line;
		var indexes = new ArrayList<Integer>();
		for (var i = 0; i < siblings.size(); i++)
			if (siblings.get(i) == marker) indexes.add(i);
		if (indexes.isEmpty()) return line;
		var copied = line.copy();
		var list = copied.getSiblings();
		for (var i = indexes.size() - 1; i >= 0; i--)
			list.remove((int) indexes.get(i));
		return copied;
	}
	public record MarkerResult<T>(T data, int indent, Component marker) {}
	/** consumeMarker 的结果：UI 数据 + 剔除 marker 后的剩余文本行（可能为 null） */
	public record MarkerSplit(TooltipComponent data, @Nullable Component remaining) {}
}
