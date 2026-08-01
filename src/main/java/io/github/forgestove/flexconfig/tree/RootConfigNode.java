package io.github.forgestove.flexconfig.tree;
import com.google.common.collect.ImmutableList;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.tree.ValueConfigNode.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
public record RootConfigNode<C, V>(String modId, ImmutableList<CategoryConfigNode<C>> categories) implements ConfigNode<C> {
	@Contract(pure = true)
	private RootConfigNode(ImmutableList<CategoryConfigNode<C>> categories, String modId) {
		this(modId, categories);
	}
	@SuppressWarnings("unchecked")
	@Contract("_, _ -> new")
	public static <C, V> @NotNull RootConfigNode<C, V> create(C defaultConfig, String modId) {
		return (RootConfigNode<C, V>) new Builder<>(defaultConfig, modId).build();
	}
	@Contract(value = " -> new", pure = true)
	public @NotNull Component getTitle() {
		return Component.empty();
	}
	@Contract(pure = true)
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
	@Override
	@NotNull
	public ImmutableList<CategoryConfigNode<C>> categories() {
		return categories;
	}
	@Nullable
	public ValueConfigNode<C, V> getValueNode(String path) {
		for (var category : categories) {
			var result = findValueNode(category, path);
			if (result != null) return result;
		}
		return null;
	}
	@Nullable
	@SuppressWarnings("unchecked")
	private ValueConfigNode<C, V> findValueNode(ConfigNode<C> node, String path) {
		if (node instanceof ValueConfigNode<C, ?> valueNode && valueNode.getPath().equals(path)) return (ValueConfigNode<C, V>) valueNode;
		if (node instanceof CategoryConfigNode<C> categoryNode) for (var child : categoryNode.getChildren()) {
			var result = findValueNode(child, path);
			if (result != null) return result;
		}
		return null;
	}
	private static class Builder<C> {
		private final String modId;
		private C defaultConfig;
		private Builder(C defaultConfig, String modId) {
			this.defaultConfig = defaultConfig;
			this.modId = modId;
		}
		private static boolean evaluatePathCondition(@NotNull List<Field> path) {
			for (var field : path)
				if (!ConfigCondition.evaluate(field)) return false;
			return true;
		}
		@Contract("_ -> param1")
		private static Field[] sortedFields(Field @NotNull [] fields) {
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
		public RootConfigNode<C, ?> build() {
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
		private @NotNull String buildPathKey(@NotNull List<Field> fields) {
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
			var fullPath = (pathKey.isEmpty() ? "" : pathKey + ".") + valueName;
			categoryBuilder.value(valueBuilder -> valueBuilder.valueType(type)
				.name(valueName)
				.path(fullPath)
				.title(Component.translatable(titleKey))
				.tooltip(Component.translatable(titleKey + ".tooltip"))
				.defaultValue(defaultValue)
				.colorValue(valueField.getAnnotation(ColorValue.class))
				.valueReader(makePathValueReader(type, path, valueField))
				.valueWriter(makePathValueWriter(type, path, valueField))
				.requiresRestart(valueField.isAnnotationPresent(RequiresRestart.class))
				.validator(FieldValidators.validatorFor(type, valueField)));
		}
		private @NotNull <V> ValueReader<C, V> makePathValueReader(Class<? extends V> type, @NotNull List<Field> path, Field valueField) {
			var pathHandles = path.stream().map(FieldAccess::varHandle).toArray(VarHandle[]::new);
			var valueHandle = FieldAccess.varHandle(valueField);
			return config -> {
				Object current = config;
				for (var handle : pathHandles) current = handle.get(current);
				return type.cast(FieldAccess.readField(valueHandle, current, valueField));
			};
		}
		private @NotNull <V> ValueWriter<C, V> makePathValueWriter(Class<? extends V> type, @NotNull List<Field> path, Field valueField) {
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
