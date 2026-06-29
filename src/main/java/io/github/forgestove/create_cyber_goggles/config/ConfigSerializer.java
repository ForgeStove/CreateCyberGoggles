package io.github.forgestove.create_cyber_goggles.config;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;

import java.awt.Point;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.function.*;
/**
 * 通用的TOML配置序列化工具，支持本地化注释。
 *
 * @param <C> 配置类类型
 */
public final class ConfigSerializer<C> {
	private final Class<C> configClass;
	private final Supplier<Path> configPath;
	private final String translationPrefix;
	private final Function<String, String> translator;
	private ConfigSerializer(Builder<C> builder) {
		configClass = builder.configClass;
		configPath = builder.configPath;
		translationPrefix = builder.translationPrefix;
		translator = builder.translator;
	}
	public static <C> Builder<C> builder(Class<C> configClass) {
		return new Builder<>(configClass);
	}
	public void serialize(C config) throws SerializationException {
		try (var fileConfig = CommentedFileConfig.builder(configPath.get()).writingMode(WritingMode.REPLACE).build()) {
			var categories = getCategoryFields();
			for (var categoryField : categories) {
				categoryField.setAccessible(true);
				var categoryName = categoryField.getName();
				var categoryValue = categoryField.get(config);
				writeCategory(fileConfig, categoryName, categoryValue, categoryName);
			}
			fileConfig.save();
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	public C deserialize() throws SerializationException {
		if (!configPath.get().toFile().exists()) return newInstance();
		try (var fileConfig = CommentedFileConfig.builder(configPath.get()).build()) {
			fileConfig.load();
			var config = newInstance();
			var fallbackValues = buildFlatValueMap(fileConfig);
			for (var categoryField : getCategoryFields()) {
				categoryField.setAccessible(true);
				readCategory(fileConfig, categoryField.getName(), categoryField.get(config), fallbackValues);
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
	private C newInstance() throws SerializationException {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new SerializationException(e);
		}
	}
	private void writeCategory(CommentedConfig config, String categoryName, Object category, String pathPrefix) throws
		IllegalAccessException {
		var subConfig = config.createSubConfig();
		var categoryComment = translate("category." + pathPrefix);
		if (categoryComment != null) config.setComment(categoryName, " " + categoryComment);
		for (var field : category.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			var fieldName = field.getName();
			var value = field.get(category);
			if (field.isAnnotationPresent(Category.class)) writeCategory(subConfig, fieldName, value, pathPrefix + "." + fieldName);
			else {
				if (value instanceof Point p) {
					var pointConfig = config.createSubConfig();
					pointConfig.set("x", p.x);
					pointConfig.set("y", p.y);
					subConfig.set(fieldName, pointConfig);
				} else if (field.isAnnotationPresent(ColorValue.class)) {
					var hasAlpha = field.getAnnotation(ColorValue.class).hasAlpha();
					subConfig.set(fieldName, new HexColorValue(hasAlpha, (Integer) value));
				} else subConfig.set(fieldName, value instanceof Enum<?> e ? e.name() : value);
				var comment = buildFieldComment(pathPrefix, fieldName);
				if (!comment.isEmpty()) subConfig.setComment(fieldName, comment);
			}
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
	/**
	 * 递归遍历整个配置树，构建字段名→值的平铺映射。
	 * 当字段被移动到不同分类路径时，可通过该映射按名称找回旧值。
	 */
	private Map<String, Object> buildFlatValueMap(CommentedConfig config) {
		var map = new HashMap<String, Object>();
		buildFlatValueMapRecursive(config, map);
		return map;
	}
	private void buildFlatValueMapRecursive(CommentedConfig config, Map<String, Object> map) {
		for (var entry : config.entrySet()) {
			var key = entry.getKey();
			var value = entry.getValue();
			map.putIfAbsent(key, value);
			if (value instanceof CommentedConfig sub) buildFlatValueMapRecursive(sub, map);
		}
	}
	@SuppressWarnings({"unchecked"})
	private <T extends Enum<T>> void readCategory(
		CommentedConfig config,
		String categoryName,
		Object category,
		Map<String, Object> fallbackValues
	) throws IllegalAccessException {
		CommentedConfig subConfig = config.get(categoryName);
		for (var field : category.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			if (field.isAnnotationPresent(Category.class)) {
				readCategory(subConfig != null ? subConfig : config, field.getName(), field.get(category), fallbackValues);
				continue;
			}
			Object value = null;
			if (subConfig != null) value = subConfig.get(field.getName());
			if (value == null && fallbackValues != null) value = fallbackValues.get(field.getName());
			if (value == null) continue;
			var type = field.getType();
			if (type == Point.class) {
				if (value instanceof CommentedConfig cc) field.set(category, new Point(cc.getInt("x"), cc.getInt("y")));
			} else if (type.isEnum()) field.set(category, Enum.valueOf((Class<T>) type, (String) value));
			else field.set(category, value);
		}
	}
	public static final class Builder<C> {
		private final Class<C> configClass;
		private Supplier<Path> configPath;
		private String translationPrefix;
		private Function<String, String> translator;
		private Builder(Class<C> configClass) {
			this.configClass = configClass;
		}
		public void path(Supplier<Path> configPath) {
			this.configPath = configPath;
		}
		public void translationPrefix(String prefix) {
			translationPrefix = prefix;
		}
		public void translator(Function<String, String> translator) {
			this.translator = translator;
		}
		public ConfigSerializer<C> build() {
			return new ConfigSerializer<>(this);
		}
	}
	public static class HexColorValue extends Number {
		public final boolean hasAlpha;
		public final int value;
		public HexColorValue(boolean hasAlpha, int value) {
			this.hasAlpha = hasAlpha;
			this.value = value;
		}
		@Override
		public String toString() {
			return hasAlpha ? String.format("0x%08X", value) : String.format("0x%06X", value);
		}
		@Override
		public int intValue() {return value;}
		@Override
		public long longValue() {return value;}
		@Override
		public float floatValue() {return value;}
		@Override
		public double doubleValue() {return value;}
	}
}
