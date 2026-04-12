package io.github.forgestove.create_cyber_goggles.config.tree;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.Objects;
public final class ValueConfigNode<C, T, V> implements ConfigNode<C> {
	private String name;
	private Component title;
	private Component tooltip;
	private Class<? extends T> type;
	private Class<? extends V> valueType;
	private boolean requiresRestart;
	private boolean isColorValue;
	private boolean colorHasAlpha;
	private ValueReader<C, V> valueReader;
	private ValueWriter<C, V> valueWriter;
	@Nullable private ValueValidator<V> validator;
	private V defaultValue;
	private V editingValue;
	private CategoryConfigNode<C> category;
	private ValueConfigNode() {
	}
	public static <C, T, V> Builder<C, T, V> builder() {
		return new Builder<>();
	}
	public Class<? extends T> getType() {
		return type;
	}
	@NotNull
	public Class<? extends V> getValueType() {
		return valueType;
	}
	public V getDefaultValue() {
		return defaultValue;
	}
	public V getActiveValue(C config) {
		return valueReader.read(config);
	}
	public void setActiveValue(C config, V value) {
		valueWriter.write(config, value);
	}
	public V getEditingValue(C config) {
		if (editingValue == null) setEditingValue(getActiveValue(config));
		return editingValue;
	}
	public void setEditingValue(V value) {
		editingValue = value;
	}
	@Override
	public void resetToDefault() {
		setEditingValue(getDefaultValue());
	}
	@Override
	public void resetToActive(C config) {
		setEditingValue(getActiveValue(config));
	}
	@Override
	public boolean isDefaultValue(C config) {
		return Objects.equals(getDefaultValue(), getEditingValue(config));
	}
	@Override
	public boolean isActiveValue(C config) {
		return Objects.equals(getActiveValue(config), getEditingValue(config));
	}
	@Override
	public Component validate(C config) {
		return validator == null ? null : validator.validate(getEditingValue(config));
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
	public boolean restartRequired(C config) {
		return requiresRestart && !isActiveValue(config);
	}
	public boolean isColorValue() {
		return isColorValue;
	}
	public boolean colorHasAlpha() {
		return colorHasAlpha;
	}
	@Override
	public void copy(C from, C to) {
		setActiveValue(to, getActiveValue(from));
	}
	@Override
	public void writeEditingToConfig(C config) {
		setActiveValue(config, getEditingValue(config));
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
	@SuppressWarnings(
		{
			"UnusedReturnValue"
		}
	)
	public static class Builder<C, T, V> {
		private ValueConfigNode<C, T, V> node;
		private Builder() {
			node = new ValueConfigNode<>();
		}
		public Builder<C, T, V> type(Class<? extends T> type) {
			node.type = type;
			return this;
		}
		public Builder<C, T, V> valueType(Class<? extends V> valueType) {
			node.valueType = valueType;
			return this;
		}
		public Builder<C, T, V> name(String name) {
			node.name = name;
			return this;
		}
		public Builder<C, T, V> title(Component title) {
			node.title = title;
			return this;
		}
		public Builder<C, T, V> tooltip(Component tooltip) {
			node.tooltip = tooltip;
			return this;
		}
		public Builder<C, T, V> defaultValue(V defaultValue) {
			node.defaultValue = defaultValue;
			return this;
		}
		public Builder<C, T, V> requiresRestart(boolean requiresRestart) {
			node.requiresRestart = requiresRestart;
			return this;
		}
		public Builder<C, T, V> colorValue(boolean isColorValue, boolean hasAlpha) {
			node.isColorValue = isColorValue;
			node.colorHasAlpha = hasAlpha;
			return this;
		}
		public Builder<C, T, V> valueReader(ValueReader<C, V> valueReader) {
			node.valueReader = valueReader;
			return this;
		}
		public Builder<C, T, V> valueWriter(ValueWriter<C, V> valueWriter) {
			node.valueWriter = valueWriter;
			return this;
		}
		public Builder<C, T, V> validator(ValueValidator<V> validator) {
			node.validator = validator;
			return this;
		}
		public Builder<C, T, V> category(CategoryConfigNode<C> category) {
			node.category = category;
			return this;
		}
		public ValueConfigNode<C, T, V> build() {
			var n = node;
			Objects.requireNonNull(n.name);
			Objects.requireNonNull(n.type);
			Objects.requireNonNull(n.valueType);
			Objects.requireNonNull(n.title);
			Objects.requireNonNull(n.valueReader);
			Objects.requireNonNull(n.valueWriter);
			Objects.requireNonNull(n.category);
			node = null;
			return n;
		}
	}
}

