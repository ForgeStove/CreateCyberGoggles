package io.github.forgestove.create_cyber_goggles.config;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;

import java.awt.Point;
import java.io.BufferedWriter;
import java.lang.reflect.Field;
import java.nio.file.*;
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
		var path = configPath.get();
		try (var writer = Files.newBufferedWriter(path)) {
			for (var categoryField : getCategoryFields()) {
				categoryField.setAccessible(true);
				writeCategoryToml(writer, categoryField.get(config), categoryField.getName(), "");
			}
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
			.sorted(Comparator.comparingInt(this::orderedFieldIndex))
			.toArray(Field[]::new);
	}
	/**
	 * Returns the fields of the given class sorted by {@link Order @Order} annotation value.
	 * Fields without {@code @Order} default to 0 and come first (preserving their
	 * {@link Class#getDeclaredFields()} relative order).
	 * Fields with an explicit positive {@code @Order} value come after, sorted by value
	 * {@link Class#getDeclaredFields() getDeclaredFields()} relative order.
	 */
	private Field[] getOrderedFields(Class<?> clazz) {
		var fields = clazz.getDeclaredFields();
		var indices = orderCache(fields);
		Arrays.sort(
			fields, Comparator.comparingInt((Field f) -> {
				var ann = f.getAnnotation(Order.class);
				return ann != null ? ann.value() : 0;
			}).thenComparingInt(f -> indices.get(f.getName()))
		);
		return fields;
	}
	private Map<String, Integer> orderCache(Field[] fields) {
		var map = new HashMap<String, Integer>();
		for (var i = 0; i < fields.length; i++) map.put(fields[i].getName(), i);
		return Map.copyOf(map);
	}
	private int orderedFieldIndex(Field f) {
		var ann = f.getAnnotation(Order.class);
		return ann != null ? ann.value() : 0;
	}
	private C newInstance() throws SerializationException {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new SerializationException(e);
		}
	}
	private void writeCategoryToml(BufferedWriter writer, Object category, String pathPrefix, String indent) throws Exception {
		var comment = translate("category." + pathPrefix);
		if (comment != null) writer.write(indent + "# " + comment.trim() + "\n");
		writer.write(indent + "[" + pathPrefix + "]\n");
		var fields = getOrderedFields(category.getClass());
		// First pass: simple values (non-Category, non-Point)
		var tables = new ArrayList<Field>();
		for (var field : fields) {
			field.setAccessible(true);
			if (field.isAnnotationPresent(Category.class) || field.getType() == Point.class) {
				tables.add(field);
				continue;
			}
			writeFieldComment(writer, indent, pathPrefix, field.getName());
			var value = field.get(category);
			if (field.isAnnotationPresent(ColorValue.class)) {
				var hasAlpha = field.getAnnotation(ColorValue.class).hasAlpha();
				var hex = hasAlpha ? String.format("0x%08X", (Integer) value) : String.format("0x%06X", (Integer) value);
				writer.write(indent + "\t" + field.getName() + " = " + hex + "\n");
			} else if (value instanceof Enum<?> e) writer.write(indent + "\t" + field.getName() + " = \"" + e.name() + "\"\n");
			else if (value instanceof String s) writer.write(indent + "\t" + field.getName() + " = \"" + s + "\"\n");
			else writer.write(indent + "\t" + field.getName() + " = " + value + "\n");
		}
		// Second pass: sub-tables (Category fields + Point fields)
		for (var field : tables) {
			field.setAccessible(true);
			writeFieldComment(writer, indent, pathPrefix, field.getName());
			var value = field.get(category);
			if (field.isAnnotationPresent(Category.class))
				writeCategoryToml(writer, value, pathPrefix + "." + field.getName(), indent + "\t");
			else {
				var p = (Point) value;
				writer.write(indent + "\t[" + pathPrefix + "." + field.getName() + "]\n");
				writer.write(indent + "\t\tx = " + p.x + "\n");
				writer.write(indent + "\t\ty = " + p.y + "\n");
			}
		}
	}
	private void writeFieldComment(BufferedWriter writer, String indent, String pathPrefix, String fieldName) throws Exception {
		var comment = buildFieldComment(pathPrefix, fieldName);
		if (comment.isEmpty()) return;
		for (var line : comment.split("\n")) {
			var trimmed = line.stripTrailing();
			if (!trimmed.isEmpty()) writer.write(indent + "\t# " + trimmed + "\n");
		}
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
		for (var field : getOrderedFields(category.getClass())) {
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
}
