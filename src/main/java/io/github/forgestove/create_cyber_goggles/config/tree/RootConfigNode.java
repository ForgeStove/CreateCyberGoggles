package io.github.forgestove.create_cyber_goggles.config.tree;
import com.google.common.collect.ImmutableList;
import io.github.forgestove.create_cyber_goggles.config.*;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
public final class RootConfigNode<C> implements ConfigNode<C> {
	public final String modId;
	private final ImmutableList<CategoryConfigNode<C>> categories;
	private RootConfigNode(ImmutableList<CategoryConfigNode<C>> categories, String modId) {
		this.categories = categories;
		this.modId = modId;
	}
	public static <C> RootConfigNode<C> create(C defaultConfig, String modId) {
		return new Builder<>(defaultConfig, modId).build();
	}
	public @NotNull Component getTitle() {
		return Component.empty();
	}
	@Override
	public @Nullable Component getTooltip() {
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
	@Override
	public @Nullable Component validate(C config) {
		Component error = null;
		for (var node : categories) {
			var result = node.validate(config);
			if (result == null) continue;
			if (error == null) error = result;
			else return Translation.MULTIPLE_ERRORS;
		}
		return error;
	}
	@Override
	public void copy(C from, C to) {
		categories.forEach(node -> node.copy(from, to));
	}
	@Override
	public void writeEditingToConfig(C config) {
		categories.forEach(node -> node.writeEditingToConfig(config));
	}
	@NotNull
	public ImmutableList<CategoryConfigNode<C>> getCategories() {
		return categories;
	}
	private static class Builder<C> {
		private final String modId;
		private C defaultConfig;
		private Builder(C defaultConfig, String modId) {
			this.defaultConfig = defaultConfig;
			this.modId = modId;
		}
		private static boolean evaluatePathCondition(List<Field> path) {
			for (var field : path)
				if (!ConfigCondition.evaluate(field)) return false;
			return true;
		}
		private static Field[] sortedFields(Field[] fields) {
			var indices = new HashMap<String, Integer>();
			for (var i = 0; i < fields.length; i++) indices.put(fields[i].getName(), i);
			Arrays.sort(
				fields,
				Comparator.comparingInt((Field f) -> f.isAnnotationPresent(Order.class) ? f.getAnnotation(Order.class).value() : 0)
					.thenComparingInt(f -> indices.get(f.getName()))
			);
			return fields;
		}
		@NotNull
		public RootConfigNode<C> build() {
			var configClass = defaultConfig.getClass();
			var categories = Arrays.stream(configClass.getFields())
				.filter(field -> field.isAnnotationPresent(Category.class))
				.filter(ConfigCondition::evaluate)
				.map(field -> Map.entry(field.isAnnotationPresent(Order.class) ? field.getAnnotation(Order.class).value() : 0, field))
				.sorted(Comparator.comparingInt(Entry::getKey))
				.map(pair -> createCategoryNode(pair.getValue()))
				.collect(ImmutableList.toImmutableList());
			defaultConfig = null;
			return new RootConfigNode<>(categories, modId);
		}
		private CategoryConfigNode<C> createCategoryNode(Field categoryField) {
			var defaultCategory = FieldAccess.getFieldValue(categoryField, defaultConfig);
			var categoryBuilder = CategoryConfigNode.<C>builder()
				.title(Component.translatable(modId + ".config.category." + categoryField.getName()));
			for (var valueField : sortedFields(categoryField.getType().getDeclaredFields()))
				if (valueField.isAnnotationPresent(Category.class)) {
					if (!ConfigCondition.evaluate(valueField)) continue;
					var subBuilder = createSubCategoryNode(List.of(categoryField), valueField);
					categoryBuilder.category(b -> subBuilder);
				} else addValueNode(defaultCategory, List.of(categoryField), valueField, categoryBuilder);
			return categoryBuilder.build();
		}
		private CategoryConfigNode.Builder<C> createSubCategoryNode(List<Field> parentPath, Field subCategoryField) {
			var path = new ArrayList<>(parentPath);
			path.add(subCategoryField);
			Object defaultParent = defaultConfig;
			for (var field : parentPath) defaultParent = FieldAccess.getFieldValue(field, defaultParent);
			var defaultSubCategory = FieldAccess.getFieldValue(subCategoryField, defaultParent);
			var annotation = subCategoryField.getAnnotation(Category.class);
			var pathKey = buildPathKey(path);
			var builder = CategoryConfigNode.<C>builder()
				.title(Component.translatable(modId + ".config.category." + pathKey))
				.defaultExpanded(annotation.value());
			for (var valueField : sortedFields(subCategoryField.getType().getDeclaredFields()))
				if (valueField.isAnnotationPresent(Category.class)) {
					if (!ConfigCondition.evaluate(valueField)) continue;
					if (!evaluatePathCondition(path)) continue;
					var subBuilder = createSubCategoryNode(path, valueField);
					builder.category(b -> subBuilder);
				} else addValueNode(defaultSubCategory, path, valueField, builder);
			return builder;
		}
		private String buildPathKey(List<Field> fields) {
			if (fields.isEmpty()) return "";
			var sb = new StringBuilder();
			for (var f : fields) {
				if (!sb.isEmpty()) sb.append('.');
				sb.append(f.getName());
			}
			return sb.toString();
		}
		private void addValueNode(
			Object defaultCategory,
			List<Field> path,
			Field valueField,
			CategoryConfigNode.Builder<C> categoryBuilder
		) {
			if (!ConfigCondition.evaluate(valueField)) return;
			if (!evaluatePathCondition(path)) return;
			var defaultValue = FieldAccess.getFieldValue(valueField, defaultCategory);
			var type = defaultValue.getClass();
			var valueName = valueField.getName();
			var pathKey = buildPathKey(path);
			var titleKey = "%s.config.option.%s.%s".formatted(modId, pathKey, valueName);
			categoryBuilder.value(valueBuilder -> valueBuilder.valueType(type)
				.name(valueName)
				.title(Component.translatable(titleKey))
				.tooltip(Component.translatable(titleKey + ".tooltip"))
				.defaultValue(defaultValue)
				.colorValue(valueField.getAnnotation(ColorValue.class))
				.valueReader(makePathValueReader(type, path, valueField))
				.valueWriter(makePathValueWriter(type, path, valueField))
				.requiresRestart(valueField.isAnnotationPresent(RequiresRestart.class))
				.validator(FieldValidators.validatorFor(type, valueField)));
		}
		private <V> ValueReader<C, V> makePathValueReader(Class<? extends V> type, List<Field> path, Field valueField) {
			var pathHandles = path.stream().map(FieldAccess::varHandle).toArray(VarHandle[]::new);
			var valueHandle = FieldAccess.varHandle(valueField);
			return config -> {
				Object current = config;
				for (var handle : pathHandles) current = handle.get(current);
				return type.cast(FieldAccess.readField(valueHandle, current, valueField));
			};
		}
		private <V> ValueWriter<C, V> makePathValueWriter(Class<? extends V> type, List<Field> path, Field valueField) {
			var pathHandles = path.stream().map(FieldAccess::varHandle).toArray(VarHandle[]::new);
			var valueHandle = FieldAccess.varHandle(valueField);
			return (config, value) -> {
				Object current = config;
				for (var handle : pathHandles) current = handle.get(current);
				FieldAccess.writeField(valueHandle, current, type.cast(value), valueField);
			};
		}
	}
}
