package io.github.forgestove.config.client.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.config.client.Translation;
import io.github.forgestove.config.client.gui.entry.*;
import io.github.forgestove.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;

import java.awt.Point;
import java.util.*;
import java.util.function.BiFunction;
public final class EntryTypeRegistry {
	private static final Map<Class<?>, EntryFactory> factories = new LinkedHashMap<>();
	static {
		register(Boolean.class, BooleanValueConfigEntry::new);
		register(
			Integer.class,
			(tab, node) -> node.isColorValue() ? new ColorValueConfigEntry<>(tab, node) : new IntegerValueConfigEntry<>(tab, node)
		);
		register(Long.class, LongValueConfigEntry::new);
		register(Float.class, FloatValueConfigEntry::new);
		register(Double.class, DoubleValueConfigEntry::new);
		register(Enum.class, EnumValueConfigEntry::new);
		register(String.class, StringValueConfigEntry::new);
		register(Key.class, KeybindValueConfigEntry::new);
		register(Point.class, PointValueConfigEntry::new);
	}
	@SuppressWarnings("unchecked")
	public static <C, V> void register(
		Class<? super V> type,
		BiFunction<ConfigCategoryTab<C, V>, ValueConfigNode<C, V>, ConfigEntry> factory
	) {
		factories.put(type, (tab, node) -> factory.apply((ConfigCategoryTab<C, V>) tab, (ValueConfigNode<C, V>) node));
	}
	public static ConfigEntry createValueEntry(ConfigCategoryTab<?, ?> tab, ValueConfigNode<?, ?> node) {
		var type = node.getValueType();
		for (var entry : factories.entrySet())
			if (entry.getKey().isAssignableFrom(type)) return entry.getValue().create(tab, node);
		return new TextConfigEntry(Translation.UNSUPPORTED_TYPE.copy().append(type.getSimpleName()).withStyle(ChatFormatting.RED));
	}
	@Contract("_, _, _, _ -> new")
	public static ConfigEntry createCategoryEntry(Component label, boolean expanded, int depth, Runnable onToggle) {
		return new CategoryCollapsibleConfigEntry(label, expanded, depth, onToggle);
	}
	@FunctionalInterface
	private interface EntryFactory {
		ConfigEntry create(ConfigCategoryTab<?, ?> tab, ValueConfigNode<?, ?> node);
	}
}
