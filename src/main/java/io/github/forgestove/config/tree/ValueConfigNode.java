package io.github.forgestove.config.tree;
import io.github.forgestove.config.api.ColorValue;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.*;
public final class ValueConfigNode<C, V> implements ConfigNode<C> {
	private String name;
	private String path;
	private Component title;
	private Component tooltip;
	private Class<? extends V> valueType;
	private boolean requiresRestart;
	@Nullable private ColorValue colorValue;
	private ValueReader<C, V> valueReader;
	private ValueWriter<C, V> valueWriter;
	@Nullable private ValueValidator<V> validator;
	private V defaultValue;
	private V editingValue;
	private CategoryConfigNode<C> category;
	public static <C, V> Builder<C, V> builder() {
		return new Builder<>();
	}
	@NotNull
	public Class<? extends V> getValueType() {
		return valueType;
	}
	@NotNull
	public Component getTitle() {
		return title;
	}
	@Nullable
	@Override
	public Component getTooltip() {
		return tooltip;
	}
	@Override
	public void resetToDefault() {
		setEditingValue(getDefaultValue());
	}
	public void setEditingValue(V value) {
		editingValue = value;
	}
	public V getDefaultValue() {
		return defaultValue;
	}
	@Override
	public void resetToActive(C config) {
		setEditingValue(getActiveValue(config));
	}
	public V getActiveValue(C config) {
		return valueReader.read(config);
	}
	@Override
	public boolean restartRequired(C config) {
		return requiresRestart && !isActiveValue(config);
	}
	@Override
	public boolean isActiveValue(C config) {
		return Objects.equals(getActiveValue(config), getEditingValue(config));
	}
	public V getEditingValue(C config) {
		if (editingValue == null) setEditingValue(getActiveValue(config));
		return editingValue;
	}
	@Override
	public boolean isDefaultValue(C config) {
		return Objects.equals(getDefaultValue(), getEditingValue(config));
	}
	@Override
	public @Nullable Component validate(C config) {
		return validator == null ? null : validator.validate(getEditingValue(config));
	}
	@Override
	public void copy(C from, C to) {
		setActiveValue(to, getActiveValue(from));
	}
	public void setActiveValue(C config, V value) {
		valueWriter.write(config, value);
	}
	@Override
	public void writeEditingToConfig(C config) {
		setActiveValue(config, getEditingValue(config));
	}
	public String getPath() {
		return path;
	}
	public boolean colorHasAlpha() {
		return isColorValue() && colorValue.value();
	}
	public boolean isColorValue() {
		return colorValue != null;
	}
	@FunctionalInterface
	public interface ValueReader<S, V> {
		V read(S source);
	}
	@FunctionalInterface
	public interface ValueWriter<S, V> {
		void write(S source, V value);
	}
	public interface ValueValidator<V> {
		@Nullable Component validate(V value);
	}
	public static class Builder<C, V> {
		private ValueConfigNode<C, V> node;
		private Builder() {
			node = new ValueConfigNode<>();
		}
		public Builder<C, V> valueType(Class<? extends V> valueType) {
			node.valueType = valueType;
			return this;
		}
		public Builder<C, V> name(String name) {
			node.name = name;
			return this;
		}
		public Builder<C, V> path(String path) {
			node.path = path;
			return this;
		}
		public Builder<C, V> title(Component title) {
			node.title = title;
			return this;
		}
		public Builder<C, V> tooltip(Component tooltip) {
			node.tooltip = tooltip;
			return this;
		}
		public Builder<C, V> defaultValue(V defaultValue) {
			node.defaultValue = defaultValue;
			return this;
		}
		public Builder<C, V> requiresRestart(boolean requiresRestart) {
			node.requiresRestart = requiresRestart;
			return this;
		}
		public Builder<C, V> colorValue(ColorValue colorValue) {
			node.colorValue = colorValue;
			return this;
		}
		public Builder<C, V> valueReader(ValueReader<C, V> valueReader) {
			node.valueReader = valueReader;
			return this;
		}
		public Builder<C, V> valueWriter(ValueWriter<C, V> valueWriter) {
			node.valueWriter = valueWriter;
			return this;
		}
		public Builder<C, V> validator(ValueValidator<V> validator) {
			node.validator = validator;
			return this;
		}
		public Builder<C, V> category(CategoryConfigNode<C> category) {
			node.category = category;
			return this;
		}
		public ValueConfigNode<C, V> build() {
			var n = node;
			List.of(n.name, n.path, n.valueType, n.title, n.valueReader, n.valueWriter, n.category).forEach(Objects::requireNonNull);
			node = null;
			return n;
		}
	}
}
