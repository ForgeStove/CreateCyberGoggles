package io.github.forgestove.create_cyber_goggles.config;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import io.github.forgestove.create_cyber_goggles.config.annotation.Category;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.function.*;
/**
 * Generic TOML configuration serializer with localized comment support.
 *
 * @param <T> Configuration class type
 */
public final class ConfigSerializer<T> {
	private final Class<T> configClass;
	private final Supplier<Path> configPath;
	private final String translationPrefix;
	private final Function<String, String> translator;
	private ConfigSerializer(Builder<T> builder) {
		this.configClass = builder.configClass;
		this.configPath = builder.configPath;
		this.translationPrefix = builder.translationPrefix;
		this.translator = builder.translator;
	}
	public static <T> Builder<T> builder(Class<T> configClass) {
		return new Builder<>(configClass);
	}
	public void serialize(T config) throws SerializationException {
		try (var fileConfig = CommentedFileConfig.builder(configPath.get()).writingMode(WritingMode.REPLACE).build()) {
			var categories = getCategoryFields();
			for (var categoryField : categories) {
				categoryField.setAccessible(true);
				var categoryName = categoryField.getName();
				var categoryValue = categoryField.get(config);
				writeCategory(fileConfig, categoryName, categoryValue);
			}
			fileConfig.save();
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	public T deserialize() throws SerializationException {
		if (!configPath.get().toFile().exists()) return newInstance();
		try (var fileConfig = CommentedFileConfig.builder(configPath.get()).build()) {
			fileConfig.load();
			var config = newInstance();
			for (var categoryField : getCategoryFields()) {
				categoryField.setAccessible(true);
				readCategory(fileConfig, categoryField.getName(), categoryField.get(config));
			}
			return config;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	private Field[] getCategoryFields() {
		return Arrays.stream(configClass.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Category.class))
			.sorted(Comparator.comparingInt(f -> f.getAnnotation(Category.class).value()))
			.toArray(Field[]::new);
	}
	private T newInstance() throws SerializationException {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new SerializationException(e);
		}
	}
	private void writeCategory(CommentedConfig config, String categoryName, Object category) throws IllegalAccessException {
		var subConfig = config.createSubConfig();
		var categoryComment = translate("category." + categoryName);
		if (categoryComment != null) config.setComment(categoryName, " " + categoryComment);
		for (var field : category.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			var fieldName = field.getName();
			var value = field.get(category);
			subConfig.set(fieldName, value instanceof Enum<?> e ? e.name() : value);
			var comment = buildFieldComment(categoryName, fieldName);
			if (!comment.isEmpty()) subConfig.setComment(fieldName, comment);
		}
		config.set(categoryName, subConfig);
	}
	private String buildFieldComment(String categorySnake, String fieldName) {
		var optionKey = "option." + categorySnake + "." + fieldName;
		var title = translate(optionKey);
		var tooltip = translate(optionKey + ".tooltip");
		var sb = new StringBuilder();
		if (title != null) sb.append(" ").append(title);
		if (tooltip != null) for (var line : tooltip.split("\n")) {
			if (!sb.isEmpty()) sb.append("\n");
			sb.append(" ").append(line);
		}
		return sb.toString();
	}
	private String translate(String key) {
		if (translator == null || translationPrefix == null) return null;
		return translator.apply(translationPrefix + "." + key);
	}
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void readCategory(CommentedConfig config, String categoryName, Object category) throws IllegalAccessException {
		CommentedConfig subConfig = config.get(categoryName);
		if (subConfig == null) return;
		for (var field : category.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			var value = subConfig.get(field.getName());
			if (value == null) continue;
			var type = field.getType();
			if (type == boolean.class || type == Boolean.class) field.setBoolean(category, (Boolean) value);
			else if (type == int.class || type == Integer.class) {
				if (value instanceof Number n) field.setInt(category, n.intValue());
			} else if (type.isEnum()) try {
				field.set(category, Enum.valueOf((Class<? extends Enum>) type, (String) value));
			} catch (IllegalArgumentException ignored) {}
		}
	}
	public static final class Builder<T> {
		private final Class<T> configClass;
		private Supplier<Path> configPath;
		private String translationPrefix;
		private Function<String, String> translator;
		private Builder(Class<T> configClass) {
			this.configClass = configClass;
		}
		public void path(Supplier<Path> configPath) {
			this.configPath = configPath;
		}
		public void translationPrefix(String prefix) {
			this.translationPrefix = prefix;
		}
		public void translator(Function<String, String> translator) {
			this.translator = translator;
		}
		public ConfigSerializer<T> build() {
			return new ConfigSerializer<>(this);
		}
	}
}
