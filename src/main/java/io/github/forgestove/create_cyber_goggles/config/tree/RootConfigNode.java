package io.github.forgestove.create_cyber_goggles.config.tree;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.forgestove.create_cyber_goggles.config.ConfigHandler;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.config.annotation.Range;
import io.github.forgestove.create_cyber_goggles.config.gui.Translation;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode.ValueValidator.None;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.util.*;
public final class RootConfigNode<C> implements ConfigNode<C> {
	private final ImmutableList<CategoryConfigNode<C>> categories;
	private RootConfigNode(ImmutableList<CategoryConfigNode<C>> categories) {
		this.categories = categories;
	}
	public static <C> RootConfigNode<C> create(C defaultConfig) {
		return new Builder<>(defaultConfig).build();
	}
	@NotNull
	@Override
	public String getName() {
		return "";
	}
	@NotNull
	@Override
	public Component getTitle() {
		return Component.empty();
	}
	@Nullable
	@Override
	public Component getTooltip() {
		return null;
	}
	@Nullable
	@Override
	public Component getPrefix() {
		return null;
	}
	@Override
	public void resetToDefault() {
		this.categories.forEach(ConfigNode::resetToDefault);
	}
	@Override
	public void resetToActive(C config) {
		this.categories.forEach(category -> category.resetToActive(config));
	}
	@Override
	public boolean restartRequired(C config) {
		return this.categories.stream().anyMatch(categoryConfigNode -> categoryConfigNode.restartRequired(config));
	}
	@Override
	public boolean isDefaultValue(C config) {
		return this.categories.stream().allMatch(node -> node.isDefaultValue(config));
	}
	@Override
	public boolean isActiveValue(C config) {
		return this.categories.stream().allMatch(node -> node.isActiveValue(config));
	}
	@Nullable
	@Override
	public Component validate(C config) {
		Component error = null;
		for (var node : this.categories) {
			var result = node.validate(config);
			if (result != null) {
				if (error != null) return CategoryConfigNode.MULTIPLE_ERRORS;
				error = result;
			}
		}
		return error;
	}
	@NotNull
	public ImmutableList<CategoryConfigNode<C>> getCategories() {
		return this.categories;
	}
	@Override
	public void copy(C from, C to) {
		this.categories.forEach(node -> node.copy(from, to));
	}
	@Override
	public void writeEditingToConfig(C config) {
		this.categories.forEach(node -> node.writeEditingToConfig(config));
	}
	private static class Builder<C> {
		private Object defaultConfig;
		private Builder(C defaultConfig) {
			this.defaultConfig = defaultConfig;
		}
		@NotNull
		public RootConfigNode<C> build() {
			var configClass = this.defaultConfig.getClass();
			var categories = Arrays.stream(configClass.getFields())
				.filter(field -> field.isAnnotationPresent(ConfigCategory.class))
				.map(field -> Pair.of(field.getAnnotation(ConfigCategory.class).value(), field))
				.sorted(Comparator.comparingInt(Pair::getFirst))
				.map(pair -> this.createCategoryNode(pair.getSecond()))
				.collect(ImmutableList.toImmutableList());
			this.defaultConfig = null;
			return new RootConfigNode<>(categories);
		}
		private CategoryConfigNode<C> createCategoryNode(Field categoryField) {
			Object defaultCategory;
			try {
				categoryField.setAccessible(true);
				defaultCategory = categoryField.get(this.defaultConfig);
			} catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
				throw new IllegalArgumentException("Failed to get category field", e);
			}
			var categoryClass = categoryField.getType();
			var categoryName = categoryField.getName();
			var categoryBuilder = CategoryConfigNode.<C>builder()
				.name(categoryName)
				.title(Component.translatable(ConfigHandler.id + ".config.category." + categoryName));
			for (var valueField : categoryClass.getDeclaredFields())
				this.addValueNode(defaultCategory, categoryField, valueField, categoryBuilder);
			return categoryBuilder.build();
		}
		private void addValueNode(
			Object defaultCategory,
			Field categoryField,
			Field valueField,
			CategoryConfigNode.Builder<C> categoryBuilder
		) {
			Object defaultValue;
			try {
				valueField.setAccessible(true);
				defaultValue = valueField.get(defaultCategory);
			} catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
				throw new IllegalArgumentException("Failed to get value field", e);
			}
			this.addSingleValueField(defaultValue.getClass(), defaultValue, categoryField, valueField, categoryBuilder);
		}
		private <T> void addSingleValueField(
			Class<? extends T> type,
			T defaultValue,
			Field categoryField,
			Field valueField,
			CategoryConfigNode.Builder<C> categoryBuilder
		) {
			var valueName = valueField.getName();
			var titleKey = ConfigHandler.id + ".config.option." + categoryField.getName() + "." + valueName;
			var title = Component.translatable(titleKey);
			var tooltip = Component.translatable(titleKey + ".tooltip");
			var prefixKey = titleKey + ".prefix";
			categoryBuilder.<T, T>value(valueBuilder -> {
				valueBuilder.type(type)
					.valueType(type)
					.name(valueName)
					.title(title)
					.tooltip(tooltip)
					.defaultValue(defaultValue)
					.valueReader(this.makeValueReader(type, categoryField, valueField))
					.valueWriter(this.makeValueWriter(type, categoryField, valueField))
					.requiresRestart(valueField.isAnnotationPresent(RequiresRestart.class));
				// Check for ColorValue annotation
				var colorAnnotation = valueField.getAnnotation(ColorValue.class);
				if (colorAnnotation != null) valueBuilder.colorValue(true, colorAnnotation.hasAlpha());
				// Check for Range annotation (handles both range and custom validator)
				var rangeAnnotation = valueField.getAnnotation(Range.class);
				if (rangeAnnotation != null) {
					var isIntegerType = type.equals(Integer.class) || type.equals(int.class);
					var hasRange = isIntegerType && (
						rangeAnnotation.min() != Integer.MIN_VALUE || rangeAnnotation.max() != Integer.MAX_VALUE
					);
					var hasCustomValidator = rangeAnnotation.validator() != None.class;
					if (hasRange) valueBuilder.range(rangeAnnotation.min(), rangeAnnotation.max());
					// Create combined validator
					ValueValidator<T> rangeValidator = hasRange ? makeRangeValidator(rangeAnnotation.min(), rangeAnnotation.max()) : null;
					ValueValidator<T> customValidator = hasCustomValidator ? makeCustomValidator(rangeAnnotation.validator()) : null;
					// Combine both validators
					if (rangeValidator != null && customValidator != null) valueBuilder.validator(v -> {
						var result = rangeValidator.validate(v);
						return result != null ? result : customValidator.validate(v);
					});
					else if (rangeValidator != null) valueBuilder.validator(rangeValidator);
					else if (customValidator != null) valueBuilder.validator(customValidator);
				}
				if (I18n.exists(prefixKey)) valueBuilder.prefix(Component.translatable(prefixKey));
				return valueBuilder;
			});
		}
		private <T> ValueValidator<T> makeRangeValidator(int min, int max) {
			return value -> {
				if (value instanceof Integer intVal) {
					if (intVal < min) return Translation.VALIDATOR_MIN.copy().append(Component.literal(String.valueOf(min)));
					if (intVal > max) return Translation.VALIDATOR_MAX.copy().append(Component.literal(String.valueOf(max)));
				}
				return null;
			};
		}
		@SuppressWarnings("unchecked")
		private <T> ValueValidator<T> makeCustomValidator(Class<? extends ValueValidator<?>> validatorClass) {
			try {
				return (ValueValidator<T>) validatorClass.getDeclaredConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw new IllegalArgumentException("Failed to create validator: " + validatorClass.getName(), e);
			}
		}
		private <T> ValueReader<C, T> makeValueReader(Class<? extends T> type, Field categoryField, Field valueField) {
			try {
				valueField.setAccessible(true);
			} catch (InaccessibleObjectException | SecurityException e) {
				throw new IllegalArgumentException("Failed to set value field accessible", e);
			}
			return (C config) -> {
				try {
					return type.cast(valueField.get(categoryField.get(config)));
				} catch (IllegalAccessException | ClassCastException e) {
					throw new IllegalArgumentException("Failed to get value field", e);
				}
			};
		}
		private <T> ValueWriter<C, T> makeValueWriter(Class<? extends T> type, Field categoryField, Field valueField) {
			try {
				valueField.setAccessible(true);
			} catch (InaccessibleObjectException | SecurityException e) {
				throw new IllegalArgumentException("Failed to set value field accessible", e);
			}
			return (C config, T value) -> {
				try {
					valueField.set(categoryField.get(config), type.cast(value));
				} catch (IllegalAccessException | ClassCastException e) {
					throw new IllegalArgumentException("Failed to set value field", e);
				}
			};
		}
	}
}
