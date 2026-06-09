package io.github.forgestove.create_cyber_goggles.config.tree;
import com.google.common.collect.ImmutableList;
import io.github.forgestove.create_cyber_goggles.config.*;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
public final class RootConfigNode<C> implements ConfigNode<C> {
	private final ImmutableList<CategoryConfigNode<C>> categories;
	public final String modId;
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
		private final String modId;
		private C defaultConfig;
		private Builder(C defaultConfig, String modId) {
			this.defaultConfig = defaultConfig;
			this.modId = modId;
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
			return new RootConfigNode<>(categories, modId);
		}
		private CategoryConfigNode<C> createCategoryNode(Field categoryField) {
			var defaultCategory = FieldAccess.getFieldValue(categoryField, defaultConfig);
			var categoryBuilder = CategoryConfigNode.<C>builder()
				.title(Component.translatable(modId + ".config.category." + categoryField.getName()));
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
			var defaultValue = FieldAccess.getFieldValue(valueField, defaultCategory);
			var type = defaultValue.getClass();
			var valueName = valueField.getName();
			var titleKey = "%s.config.option.%s.%s".formatted(modId, categoryField.getName(), valueName);
			categoryBuilder.value(valueBuilder -> valueBuilder.valueType(type)
				.name(valueName)
				.title(Component.translatable(titleKey))
				.tooltip(Component.translatable(titleKey + ".tooltip"))
				.defaultValue(defaultValue)
				.colorValue(valueField.getAnnotation(ColorValue.class))
				.valueReader(makeValueReader(type, categoryField, valueField))
				.valueWriter(makeValueWriter(type, categoryField, valueField))
				.requiresRestart(valueField.isAnnotationPresent(RequiresRestart.class))
				.validator(FieldValidators.validatorFor(type, valueField)));
		}
		private <V> ValueReader<C, V> makeValueReader(Class<? extends V> type, Field categoryField, Field valueField) {
			var categoryHandle = FieldAccess.varHandle(categoryField);
			var valueHandle = FieldAccess.varHandle(valueField);
			return config -> type.cast(FieldAccess.readField(valueHandle, categoryHandle.get(config), valueField));
		}
		private <V> ValueWriter<C, V> makeValueWriter(Class<? extends V> type, Field categoryField, Field valueField) {
			var categoryHandle = FieldAccess.varHandle(categoryField);
			var valueHandle = FieldAccess.varHandle(valueField);
			return (config, value) -> FieldAccess.writeField(valueHandle, categoryHandle.get(config), type.cast(value), valueField);
		}
	}
}
