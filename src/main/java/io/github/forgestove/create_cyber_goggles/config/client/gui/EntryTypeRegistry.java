package io.github.forgestove.create_cyber_goggles.config.client.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.create_cyber_goggles.config.client.Translation;
import io.github.forgestove.create_cyber_goggles.config.client.gui.entry.*;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.*;

import java.awt.*;
import java.util.Map;
public final class EntryTypeRegistry<C> {
	private final Map<Class<?>, EntryFactory<C>> factories = Map.of(
		Enum.class,
		(tab, node) -> new EnumValueConfigEntry<>(tab, cast(node)),
		Boolean.class,
		(tab, node) -> new BooleanValueConfigEntry<>(tab, cast(node)),
		Integer.class,
		(tab, node) -> node.isColorValue() ? new ColorValueConfigEntry<>(tab, cast(node)) : new IntegerValueConfigEntry<>(tab, cast(node)),
		Long.class,
		(tab, node) -> new LongValueConfigEntry<>(tab, cast(node)),
		Float.class,
		(tab, node) -> new FloatValueConfigEntry<>(tab, cast(node)),
		Double.class,
		(tab, node) -> new DoubleValueConfigEntry<>(tab, cast(node)),
		String.class,
		(tab, node) -> new StringValueConfigEntry<>(tab, cast(node)),
		Color.class,
		(tab, node) -> new ColorValueConfigEntry<>(tab, cast(node)),
		Key.class,
		(tab, node) -> new KeybindValueConfigEntry<>(tab, cast(node)),
		Point.class,
		(tab, node) -> new PointValueConfigEntry<>(tab, cast(node))
	);
	@Contract(value = "_ -> param1", pure = true)
	@SuppressWarnings("unchecked")
	private static <C, V> ValueConfigNode<C, V> cast(ValueConfigNode<C, ?> node) {
		return (ValueConfigNode<C, V>) node;
	}
	public ConfigEntry createValueEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, ?> node) {
		var type = node.getValueType();
		for (var entry : factories.entrySet())
			if (entry.getKey().isAssignableFrom(type)) return entry.getValue().create(tab, node);
		return new TextConfigEntry(tab, Translation.UNSUPPORTED_TYPE.copy().append(type.getSimpleName()).withStyle(ChatFormatting.RED));
	}
	@Contract("_, _, _, _, _ -> new")
	public @NotNull ConfigEntry createCategoryEntry(
		ConfigCategoryTab<C> tab,
		CategoryConfigNode<C> node,
		boolean expanded,
		int depth,
		Runnable onToggle
	) {
		return new CategoryCollapsibleConfigEntry(tab, node, expanded, depth, onToggle);
	}
	@FunctionalInterface
	private interface EntryFactory<C> {
		ConfigEntry create(ConfigCategoryTab<C> tab, ValueConfigNode<C, ?> node);
	}
}
