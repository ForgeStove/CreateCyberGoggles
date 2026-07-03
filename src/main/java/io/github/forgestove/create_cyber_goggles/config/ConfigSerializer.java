package io.github.forgestove.create_cyber_goggles.config;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.config.client.Translation;
import net.neoforged.fml.loading.FMLPaths;

import java.awt.Point;
import java.io.BufferedWriter;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
/**
 * 通用的TOML配置序列化工具，支持本地化注释。
 *
 * @param <C> 配置类类型
 */
public final class ConfigSerializer<C> {
	private final Class<C> configClass;
	private final Path configPath;
	private final String translationPrefix;
	private ConfigSerializer(Builder<C> builder) {
		configClass = builder.configClass;
		var modId = ConfigRegistry.getModId(configClass);
		configPath = FMLPaths.CONFIGDIR.get().resolve(modId + ".toml");
		translationPrefix = modId + ".config";
	}
	public static <C> Builder<C> builder(Class<C> configClass) {
		return new Builder<>(configClass);
	}
	public void serialize(C config) throws SerializationException {
		try (var writer = Files.newBufferedWriter(configPath)) {
			for (var categoryField : getCategoryFields()) {
				categoryField.setAccessible(true);
				writeCategoryToml(writer, categoryField.get(config), categoryField.getName());
			}
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	public C deserialize() throws SerializationException {
		if (!configPath.toFile().exists()) return newInstance();
		try (var fileConfig = CommentedFileConfig.builder(configPath).build()) {
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
	/**
	 * 从已解析的 {@link CommentedConfig} 反序列化配置实例，不从文件读取。
	 * 供网络包等场景使用，复用相同的字段映射和类型转换逻辑。
	 */
	public C deserializeFrom(CommentedConfig config) throws SerializationException {
		try {
			var instance = newInstance();
			var fallbackValues = buildFlatValueMap(config);
			for (var categoryField : getCategoryFields()) {
				categoryField.setAccessible(true);
				readCategory(config, categoryField.getName(), categoryField.get(instance), fallbackValues);
			}
			return instance;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	private Field[] getCategoryFields() {
		List<Field> list = new ArrayList<>();
		for (var field : getOrderedFields(configClass)) if (field.isAnnotationPresent(Category.class)) list.add(field);
		return list.toArray(new Field[0]);
	}
	/**
	 * 对给定类的字段按 {@link Order @Order} 注解值排序。
	 * 无 {@code @Order} 的字段默认值为 0，排在前面（保持 {@link Class#getDeclaredFields()} 的相对顺序）。
	 * 有显式正数 {@code @Order} 值的字段在后，先按值排序再按声明顺序排序。
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
		return map;
	}
	private C newInstance() throws SerializationException {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new SerializationException(e);
		}
	}
	private void writeCategoryToml(BufferedWriter writer, Object category, String pathPrefix) throws Exception {
		var comment = translate("category." + pathPrefix);
		if (comment != null) writer.write("# %s\n".formatted(comment.trim()));
		writer.write("[%s]\n".formatted(pathPrefix));
		var fields = getOrderedFields(category.getClass());
		for (var field : fields) {
			field.setAccessible(true);
			if (field.isAnnotationPresent(Category.class)) {
				writeFieldComment(writer, pathPrefix, field.getName());
				writeCategoryToml(writer, field.get(category), "%s.%s".formatted(pathPrefix, field.getName()));
				continue;
			}
			writeFieldComment(writer, pathPrefix, field.getName());
			var value = field.get(category);
			if (field.isAnnotationPresent(ColorValue.class)) {
				var hasAlpha = field.getAnnotation(ColorValue.class).value();
				var hex = (hasAlpha ? "0x%08X" : "0x%06X").formatted(value);
				writer.write("\t%s = %s\n".formatted(field.getName(), hex));
			} else switch (value) {
				case Enum<?> e -> writer.write("\t%s = \"%s\"\n".formatted(field.getName(), e.name()));
				case Point p -> writer.write("\t%s = \"%d, %d\"\n".formatted(field.getName(), p.x, p.y));
				case String s -> writer.write("\t%s = \"%s\"\n".formatted(field.getName(), s));
				default -> writer.write("\t%s = %s\n".formatted(field.getName(), value));
			}
		}
	}
	private void writeFieldComment(BufferedWriter writer, String pathPrefix, String fieldName) throws Exception {
		var comment = buildFieldComment(pathPrefix, fieldName);
		if (comment.isEmpty()) return;
		for (var line : comment.split("\n")) {
			var trimmed = line.stripTrailing();
			if (!trimmed.isEmpty()) writer.write("\t# %s\n".formatted(trimmed));
		}
	}
	private String buildFieldComment(String categorySnake, String fieldName) {
		var optionKey = "option.%s.%s".formatted(categorySnake, fieldName);
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
		if (translationPrefix == null) return null;
		key = "%s.%s".formatted(translationPrefix, key);
		return Translation.getString(key);
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
			if (value instanceof CommentedConfig sub) buildFlatValueMapRecursive(sub, map);
			else map.putIfAbsent(key, value);
		}
	}
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void readCategory(CommentedConfig config, String categoryName, Object category, Map<String, Object> fallbackValues) throws
		IllegalAccessException {
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
				if (value instanceof String s) {
					var parts = s.split(",");
					try {
						field.set(category, new Point(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())));
					} catch (NumberFormatException ignored) {}
				} else if (value instanceof CommentedConfig cc) field.set(category, new Point(cc.getInt("x"), cc.getInt("y")));
			} else if (type.isEnum()) field.set(category, Enum.valueOf((Class) type, (String) value));
			else field.set(category, value);
		}
	}
	public static final class Builder<C> {
		private final Class<C> configClass;
		private Builder(Class<C> configClass) {
			this.configClass = configClass;
		}
		public ConfigSerializer<C> build() {
			return new ConfigSerializer<>(this);
		}
	}
}
