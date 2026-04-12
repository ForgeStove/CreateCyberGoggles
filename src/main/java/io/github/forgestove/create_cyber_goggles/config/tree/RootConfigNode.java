package io.github.forgestove.create_cyber_goggles.config.tree;
import com.google.common.collect.ImmutableList;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
public final class RootConfigNode<C> implements ConfigNode<C> {
	private final ImmutableList<CategoryConfigNode<C>> categories;
	private RootConfigNode(ImmutableList<CategoryConfigNode<C>> categories) {
		this.categories = categories;
	}
	public static <C> RootConfigNode<C> create(C defaultConfig, String modId) {
		return new Builder<>(defaultConfig, modId).build();
	}
	@NotNull
	public Component getTitle() {
		return Component.empty();
	}
	@Nullable
	@Override
	public Component getTooltip() {
		return null;
	}
	@Override
	public void resetToDefault() {
		categories.forEach(ConfigNode::resetToDefault);
	}
	@Override
	public void resetToActive(C config) {
		categories.forEach(category -> category.resetToActive(config));
	}
	@Override
	public boolean restartRequired(C config) {
		return categories.stream().anyMatch(categoryConfigNode -> categoryConfigNode.restartRequired(config));
	}
	@Override
	public boolean isDefaultValue(C config) {
		return categories.stream().allMatch(node -> node.isDefaultValue(config));
	}
	@Override
	public boolean isActiveValue(C config) {
		return categories.stream().allMatch(node -> node.isActiveValue(config));
	}
	@Nullable
	@Override
	public Component validate(C config) {
		Component error = null;
		for (var node : categories) {
			var result = node.validate(config);
			if (result != null) {
				if (error != null) return Translation.MULTIPLE_ERRORS;
				error = result;
			}
		}
		return error;
	}
	@NotNull
	public ImmutableList<CategoryConfigNode<C>> getCategories() {
		return categories;
	}
	@Override
	public void copy(C from, C to) {
		categories.forEach(node -> node.copy(from, to));
	}
	@Override
	public void writeEditingToConfig(C config) {
		categories.forEach(node -> node.writeEditingToConfig(config));
	}
	private static class Builder<C> {
		private static final Map<Class<?>, ValidatorFactory<?>> VALIDATOR_FACTORIES = Map.of(
			Integer.class, (ValueConfigNode.Builder<?, Integer, Integer> b, Field f) -> {
				var range = f.getAnnotation(IntRange.class);
				if (range != null) b.validator(makeRangeValidator(v -> v < range.min(), v -> v > range.max(), range.min(), range.max()));
			}, int.class, (ValueConfigNode.Builder<?, Integer, Integer> b, Field f) -> {
				var range = f.getAnnotation(IntRange.class);
				if (range != null) b.validator(makeRangeValidator(v -> v < range.min(), v -> v > range.max(), range.min(), range.max()));
			}, Long.class, (ValueConfigNode.Builder<?, Long, Long> b, Field f) -> {
				var range = f.getAnnotation(LongRange.class);
				if (range != null) b.validator(makeRangeValidator(v -> v < range.min(), v -> v > range.max(), range.min(), range.max()));
			}, long.class, (ValueConfigNode.Builder<?, Long, Long> b, Field f) -> {
				var range = f.getAnnotation(LongRange.class);
				if (range != null) b.validator(makeRangeValidator(v -> v < range.min(), v -> v > range.max(), range.min(), range.max()));
			}, Float.class, (ValueConfigNode.Builder<?, Float, Float> b, Field f) -> {
				var range = f.getAnnotation(FloatRange.class);
				if (range != null) b.validator(makeRangeValidator(v -> v < range.min(), v -> v > range.max(), range.min(), range.max()));
			}, float.class, (ValueConfigNode.Builder<?, Float, Float> b, Field f) -> {
				var range = f.getAnnotation(FloatRange.class);
				if (range != null) b.validator(makeRangeValidator(v -> v < range.min(), v -> v > range.max(), range.min(), range.max()));
			}, Double.class, (ValueConfigNode.Builder<?, Double, Double> b, Field f) -> {
				var range = f.getAnnotation(DoubleRange.class);
				if (range != null) b.validator(makeRangeValidator(v -> v < range.min(), v -> v > range.max(), range.min(), range.max()));
			}, double.class, (ValueConfigNode.Builder<?, Double, Double> b, Field f) -> {
				var range = f.getAnnotation(DoubleRange.class);
				if (range != null) b.validator(makeRangeValidator(v -> v < range.min(), v -> v > range.max(), range.min(), range.max()));
			}, String.class, (ValueConfigNode.Builder<?, String, String> b, Field f) -> {
				var strLen = f.getAnnotation(StringLength.class);
				if (strLen != null) b.validator(makeRangeValidator(
					v -> v.length() < strLen.min(),
					v -> v.length() > strLen.max(),
					strLen.min(),
					strLen.max()
				));
			}
		);
		private final String id;
		private Object defaultConfig;
		private Builder(C defaultConfig, String id) {
			this.defaultConfig = defaultConfig;
			this.id = id;
		}
		private static <T> ValueValidator<T> makeRangeValidator(Predicate<T> belowMin, Predicate<T> aboveMax, Object min, Object max) {
			return value -> {
				if (belowMin.test(value)) return Translation.VALIDATOR_MIN.copy().append(String.valueOf(min));
				if (aboveMax.test(value)) return Translation.VALIDATOR_MAX.copy().append(String.valueOf(max));
				return null;
			};
		}
		@NotNull
		public RootConfigNode<C> build() {
			var configClass = defaultConfig.getClass();
			var categories = Arrays.stream(configClass.getFields())
				.filter(field -> field.isAnnotationPresent(Category.class))
				.map(field -> Map.entry(field.getAnnotation(Category.class).value(), field))
				.sorted(Comparator.comparingInt(Entry::getKey))
				.map(pair -> createCategoryNode(pair.getValue()))
				.collect(ImmutableList.toImmutableList());
			defaultConfig = null;
			return new RootConfigNode<>(categories);
		}
		private CategoryConfigNode<C> createCategoryNode(Field categoryField) {
			Object defaultCategory;
			try {
				categoryField.setAccessible(true);
				defaultCategory = categoryField.get(defaultConfig);
			} catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
				throw new IllegalArgumentException("Failed to get category field", e);
			}
			var categoryClass = categoryField.getType();
			var categoryName = categoryField.getName();
			var categoryBuilder = CategoryConfigNode.<C>builder()
				.name(categoryName)
				.title(Component.translatable(id + ".config.category." + categoryName));
			for (var valueField : categoryClass.getDeclaredFields())
				addValueNode(defaultCategory, categoryField, valueField, categoryBuilder);
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
			addSingleValueField(defaultValue.getClass(), defaultValue, categoryField, valueField, categoryBuilder);
		}
		@SuppressWarnings("unchecked")
		private <T> void addSingleValueField(
			Class<? extends T> type,
			T defaultValue,
			Field categoryField,
			Field valueField,
			CategoryConfigNode.Builder<C> categoryBuilder
		) {
			var valueName = valueField.getName();
			var titleKey = id + ".config.option." + categoryField.getName() + "." + valueName;
			var title = Component.translatable(titleKey);
			var tooltip = Component.translatable(titleKey + ".tooltip");
			categoryBuilder.<T, T>value(valueBuilder -> {
				valueBuilder.type(type)
					.valueType(type)
					.name(valueName)
					.title(title)
					.tooltip(tooltip)
					.defaultValue(defaultValue)
					.valueReader(makeValueReader(type, categoryField, valueField))
					.valueWriter(makeValueWriter(type, categoryField, valueField))
					.requiresRestart(valueField.isAnnotationPresent(RequiresRestart.class));
				// Check for ColorValue annotation
				var colorAnnotation = valueField.getAnnotation(ColorValue.class);
				if (colorAnnotation != null) valueBuilder.colorValue(true, colorAnnotation.hasAlpha());
				// Look up validator factory from the table
				var factory = (ValidatorFactory<T>) VALIDATOR_FACTORIES.get(type);
				if (factory != null) factory.apply(valueBuilder, valueField);
				return valueBuilder;
			});
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
		@FunctionalInterface
		private interface ValidatorFactory<T> {
			void apply(ValueConfigNode.Builder<?, T, T> builder, Field valueField);
		}
	}
}
