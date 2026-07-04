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
	@SuppressWarnings("unchecked") private final Map<Class<?>, EntryFactory> factories = Map.of(
		Enum.class,
		(tab, node) -> new EnumValueConfigEntry<>((ConfigCategoryTab<C, Enum<?>>) tab, (ValueConfigNode<C, Enum<?>>) node),
		Boolean.class,
		(tab, node) -> new BooleanValueConfigEntry<>((ConfigCategoryTab<C, Boolean>) tab, (ValueConfigNode<C, Boolean>) node),
		Integer.class,
		(tab, node) -> node.isColorValue() ? new ColorValueConfigEntry<>(
			(ConfigCategoryTab<C, Integer>) tab,
			(ValueConfigNode<C, Integer>) node
		) : new IntegerValueConfigEntry<>((ConfigCategoryTab<C, Integer>) tab, (ValueConfigNode<C, Integer>) node),
		Long.class,
		(tab, node) -> new LongValueConfigEntry<>((ConfigCategoryTab<C, Long>) tab, (ValueConfigNode<C, Long>) node),
		Float.class,
		(tab, node) -> new FloatValueConfigEntry<>((ConfigCategoryTab<C, Float>) tab, (ValueConfigNode<C, Float>) node),
		Double.class,
		(tab, node) -> new DoubleValueConfigEntry<>((ConfigCategoryTab<C, Double>) tab, (ValueConfigNode<C, Double>) node),
		String.class,
		(tab, node) -> new StringValueConfigEntry<>((ConfigCategoryTab<C, String>) tab, (ValueConfigNode<C, String>) node),
		Color.class,
		(tab, node) -> new ColorValueConfigEntry<>((ConfigCategoryTab<C, Integer>) tab, (ValueConfigNode<C, Integer>) node),
		Key.class,
		(tab, node) -> new KeybindValueConfigEntry<>((ConfigCategoryTab<C, Key>) tab, (ValueConfigNode<C, Key>) node),
		Point.class,
		(tab, node) -> new PointValueConfigEntry<>((ConfigCategoryTab<C, Point>) tab, (ValueConfigNode<C, Point>) node)
	);
	public ConfigEntry createValueEntry(ConfigCategoryTab<C, ?> tab, ValueConfigNode<C, ?> node) {
		var type = node.getValueType();
		for (var entry : factories.entrySet())
			if (entry.getKey().isAssignableFrom(type)) return entry.getValue().create(tab, node);
		return new TextConfigEntry(Translation.UNSUPPORTED_TYPE.copy().append(type.getSimpleName()).withStyle(ChatFormatting.RED));
	}
	@Contract("_, _, _, _ -> new")
	public @NotNull ConfigEntry createCategoryEntry(CategoryConfigNode<C> node, boolean expanded, int depth, Runnable onToggle) {
		return new CategoryCollapsibleConfigEntry(node, expanded, depth, onToggle);
	}
	@FunctionalInterface
	private interface EntryFactory {
		ConfigEntry create(ConfigCategoryTab<?, ?> tab, ValueConfigNode<?, ?> node);
	}
}
