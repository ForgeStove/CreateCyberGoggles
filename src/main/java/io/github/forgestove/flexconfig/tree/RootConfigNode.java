package io.github.forgestove.flexconfig.tree;
import com.google.common.collect.ImmutableList;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.platform.Platform;
import io.github.forgestove.flexconfig.tree.ValueConfigNode.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.*;
public record RootConfigNode<C, V>(
	String modId, String fileName, ImmutableList<CategoryConfigNode<C>> categories, ImmutableList<String> translationKeys
) implements ConfigNode<C> {
	@SuppressWarnings("unchecked")
	@Contract("_, _, _, _ -> new")
	public static <C, V> @NotNull RootConfigNode<C, V> create(C defaultConfig, String modId, String fileName, ConfigType type) {
		return (RootConfigNode<C, V>) new Builder<>(defaultConfig, modId, fileName, type).build();
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
	@Nullable
	@Override
	public RestartType restartType(C config) {
		RestartType worst = null;
		for (var category : categories) {
			var type = category.restartType(config);
			if (type == RestartType.GAME) return RestartType.GAME;   // 取最重的一档
			if (type == RestartType.WORLD) worst = RestartType.WORLD;
		}
		return worst;
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
	public void copyForRuntime(C from, C to) {
		categories.forEach(node -> node.copyForRuntime(from, to));
	}
	@Override
	public void flushPending(C to) {
		categories.forEach(node -> node.flushPending(to));
	}
	@Override
	public void applyRestart(C saved, C active, RestartType restartType) {
		ConfigChangeDispatcher.begin();
		try {
			categories.forEach(node -> node.applyRestart(saved, active, restartType));
		} finally {
			ConfigChangeDispatcher.end();   // finally：节点抛异常也不会让派发深度卡住
		}
	}
	@Override
	public void writeEditingToConfig(C config) {
		ConfigChangeDispatcher.begin();
		try {
			categories.forEach(node -> node.writeEditingToConfig(config));
		} finally {
			ConfigChangeDispatcher.end();   // finally：节点抛异常也不会让派发深度卡住
		}
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
		private final String fileName;
		private final ConfigType type;
		/** 构建过程中收集到的全部翻译键，供开发期检查缺失。 */
		private final ImmutableList.Builder<String> translationKeys = ImmutableList.builder();
		private C defaultConfig;
		private Builder(C defaultConfig, String modId, String fileName, ConfigType type) {
			this.defaultConfig = defaultConfig;
			this.modId = modId;
			this.fileName = fileName;
			this.type = type;
		}
		private static boolean evaluatePathCondition(@NotNull List<Field> path) {
			for (var field : path)
				if (!ConfigCondition.evaluate(field)) return false;
			return true;
		}
		@NotNull
		public RootConfigNode<C, ?> build() {
			var categories = Arrays.stream(FieldAccess.collectOrderedFields(defaultConfig.getClass()))
				.filter(field -> field.isAnnotationPresent(Category.class))
				.filter(ConfigCondition::evaluate)
				.map(this::createCategoryNode)
				.collect(ImmutableList.toImmutableList());
			defaultConfig = null;
			return new RootConfigNode<>(modId, fileName, categories, translationKeys.build());
		}
		private CategoryConfigNode<C> createCategoryNode(Field categoryField) {
			var defaultCategory = FieldAccess.getFieldValue(categoryField, defaultConfig);
			var categoryKey = modId + ".config.category." + categoryField.getName();
			translationKeys.add(categoryKey);
			var categoryBuilder = CategoryConfigNode.<C>builder().title(Component.translatable(categoryKey));
			for (var valueField : FieldAccess.collectOrderedFields(categoryField.getType()))
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
			var categoryKey = modId + ".config.category." + pathKey;
			translationKeys.add(categoryKey);
			var builder = CategoryConfigNode.<C>builder().title(Component.translatable(categoryKey)).defaultExpanded(annotation.value());
			for (var valueField : FieldAccess.collectOrderedFields(subCategoryField.getType()))
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
			var type = FieldAccess.boxed(valueField.getType());
			var elementType = FieldAccess.elementType(valueField);
			var valueName = valueField.getName();
			var pathKey = buildPathKey(path);
			var titleKey = "%s.config.option.%s.%s".formatted(modId, pathKey, valueName);
			translationKeys.add(titleKey);
			translationKeys.add(titleKey + ".tooltip");
			// 枚举值的显示键：<modId>.config.enum.<枚举类名>.<常量名>
			if (type.isEnum()) for (var constant : type.getEnumConstants())
				translationKeys.add("%s.config.enum.%s.%s".formatted(modId, type.getSimpleName(), ((Enum<?>) constant).name()));
			var fullPath = (pathKey.isEmpty() ? "" : pathKey + ".") + valueName;
			var tooltip = Component.translatable(titleKey + ".tooltip");
			if (valueField.isAnnotationPresent(WarnCheat.class)) tooltip = tooltip.copy()
				.append(Component.literal("\n"))
				.append(Component.translatable("config.ui.warn_cheat.tooltip").withStyle(ChatFormatting.RED));
			final var finalTooltip = tooltip;
			categoryBuilder.value(valueBuilder -> valueBuilder.valueType(type)
				.name(valueName)
				.path(fullPath)
				.title(Component.translatable(titleKey))
				.tooltip(finalTooltip)
				.defaultValue(defaultValue)
				.colorValue(valueField.getAnnotation(ColorValue.class))
				.elementType(elementType)
				.valueReader(makePathValueReader(type, path, valueField))
				.valueWriter(makePathValueWriter(type, path, valueField))
				.restartType(restartTypeOf(valueField))
				.changeCallback(makeChangeCallback(valueField, fullPath))
				.validator(FieldValidators.validatorFor(type, valueField)));
		}
		/** 读取 {@link RequiresRestart} 声明的重启级别并校验它适用于本配置类型；未标注返回 {@code null}。 */
		private @Nullable RestartType restartTypeOf(Field field) {
			var annotation = field.getAnnotation(RequiresRestart.class);
			if (annotation == null) return null;
			var restartType = annotation.value();
			if (!restartType.isValid(type)) throw new IllegalArgumentException(
				"Option '%s' of %s config %s declares restart type %s, which cannot be used for that config type".formatted(
					field.getName(),
					type,
					fileName,
					restartType
				));
			return restartType;
		}
		private @Nullable ConfigChangeCallback makeChangeCallback(Field valueField, String path) {
			if (!Platform.get().isClient()) return null;
			var annotation = valueField.getAnnotation(OnChange.class);
			if (annotation == null) return null;
			var type = annotation.value();
			return (oldValue, newValue) -> ConfigChangeDispatcher.record(type, path, oldValue, newValue);
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
