package io.github.forgestove.create_cyber_goggles.config.tree;
import com.google.common.collect.ImmutableList;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.invoke.*;
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
			if (result == null) continue;
			if (error == null) error = result;
			else return Translation.MULTIPLE_ERRORS;
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
			Integer.class, (ValidatorFactory<Integer>) (b, f) -> {
				var range = f.getAnnotation(IntRange.class);
				if (range != null) b.validator(makeRangeValidator(
					(Integer v) -> v < range.min(),
					(Integer v) -> v > range.max(),
					range.min(),
					range.max()
				));
			}, Long.class, (ValidatorFactory<Long>) (b, f) -> {
				var range = f.getAnnotation(LongRange.class);
				if (range != null)
					b.validator(makeRangeValidator((Long v) -> v < range.min(), (Long v) -> v > range.max(), range.min(), range.max()));
			}, Float.class, (ValidatorFactory<Float>) (b, f) -> {
				var range = f.getAnnotation(FloatRange.class);
				if (range != null)
					b.validator(makeRangeValidator((Float v) -> v < range.min(), (Float v) -> v > range.max(), range.min(), range.max()));
			}, Double.class, (ValidatorFactory<Double>) (b, f) -> {
				var range = f.getAnnotation(DoubleRange.class);
				if (range != null)
					b.validator(makeRangeValidator((Double v) -> v < range.min(), (Double v) -> v > range.max(), range.min(),
						range.max()));
			}, String.class, (ValidatorFactory<String>) (b, f) -> {
				var strLen = f.getAnnotation(StringLength.class);
				if (strLen != null) b.validator(makeRangeValidator(
					(String v) -> v.length() < strLen.min(),
					(String v) -> v.length() > strLen.max(),
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
		private static VarHandle varHandle(Field field) {
			try {
				return MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectVarHandle(field);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Cannot access field: " + field.getName(), e);
			}
		}
		private static Object getFieldValue(Field field, Object target) {
			try {
				field.setAccessible(true);
				return field.get(target);
			} catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
				throw new IllegalArgumentException("Failed to access field: " + field.getName(), e);
			}
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
			var defaultCategory = getFieldValue(categoryField, defaultConfig);
			var categoryBuilder = CategoryConfigNode.<C>builder()
				.title(Component.translatable(id + ".config.category." + categoryField.getName()));
			for (var valueField : categoryField.getType().getDeclaredFields())
				addValueNode(defaultCategory, categoryField, valueField, categoryBuilder);
			return categoryBuilder.build();
		}
		private void addValueNode(
			Object defaultCategory,
			Field categoryField,
			Field valueField,
			CategoryConfigNode.Builder<C> categoryBuilder
		) {
			var defaultValue = getFieldValue(valueField, defaultCategory);
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
			categoryBuilder.<T, T>value(valueBuilder -> {
				valueBuilder.type(type)
					.valueType(type)
					.name(valueName)
					.title(Component.translatable(titleKey))
					.tooltip(Component.translatable(titleKey + ".tooltip"))
					.defaultValue(defaultValue)
					.valueReader(makeValueReader(type, categoryField, valueField))
					.valueWriter(makeValueWriter(type, categoryField, valueField))
					.requiresRestart(valueField.isAnnotationPresent(RequiresRestart.class));
				var colorAnnotation = valueField.getAnnotation(ColorValue.class);
				if (colorAnnotation != null) valueBuilder.colorValue(true, colorAnnotation.hasAlpha());
				var factory = (ValidatorFactory<T>) VALIDATOR_FACTORIES.get(type);
				if (factory != null) factory.apply(valueBuilder, valueField);
				return valueBuilder;
			});
		}
		private <T> ValueReader<C, T> makeValueReader(Class<? extends T> type, Field categoryField, Field valueField) {
			var catHandle = varHandle(categoryField);
			var valHandle = varHandle(valueField);
			return config -> {
				try {
					return type.cast(valHandle.get(catHandle.get(config)));
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Failed to read " + valueField.getName(), e);
				}
			};
		}
		private <T> ValueWriter<C, T> makeValueWriter(Class<? extends T> type, Field categoryField, Field valueField) {
			var catHandle = varHandle(categoryField);
			var valHandle = varHandle(valueField);
			return (config, value) -> {
				try {
					valHandle.set(catHandle.get(config), type.cast(value));
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Failed to write " + valueField.getName(), e);
				}
			};
		}
		@FunctionalInterface
		private interface ValidatorFactory<T> {
			void apply(ValueConfigNode.Builder<?, T, T> builder, Field valueField);
		}
	}
}
