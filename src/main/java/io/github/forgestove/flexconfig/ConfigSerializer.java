package io.github.forgestove.flexconfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.Translation;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.Point;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
/**
 * 通用的TOML配置序列化工具，支持本地化注释。
 * <p>
 * 写入交给 NightConfig 的 {@link TomlWriter}（{@link WritingMode#REPLACE_ATOMIC} 先写临时文件再原子替换），
 * 因此键名/字符串转义、数值格式都不需要自己维护。
 *
 * @param <C> 配置类类型
 */
public final class ConfigSerializer<C> {
	/** 快捷键段名（合法 bare key）。 */
	private static final String KEYBINDS_SECTION = "-keybinds-";
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Class<C> configClass;
	private final String translationPrefix;
	/** 读写的目标文件，由 {@link ConfigHandler} 在解析路径后注入。 */
	private Path targetPath;
	private ConfigSerializer(Builder<C> builder) {
		configClass = builder.configClass;
		translationPrefix = ConfigRegistry.getModId(configClass) + ".config";
	}
	public static <C> Builder<C> builder(Class<C> configClass) {
		return new Builder<>(configClass);
	}
	/** 新建一个保持插入顺序的内存配置。 */
	public static CommentedConfig inMemory() {
		return CommentedConfig.of(LinkedHashMap::new, InMemoryCommentedFormat.defaultInstance());
	}
	/** 把配置值转成 NightConfig 可写入的表示；不支持或非法的值返回 {@code null}（跳过该条）。 */
	private static @Nullable Object toTomlValue(Field field, @Nullable Object value) {
		if (value == null) return warnNull(field, "value is null");
		if (field.isAnnotationPresent(ColorValue.class) && !(value instanceof Integer))
			return warnNull(field, "color value is not an integer");
		if (value instanceof Point point) return pointConfig(point);
		if (value instanceof List<?> list) return toTomlList(field, list);
		return scalar(field, value);
	}
	/** 列表元素转成可写入的表示；有一个元素不支持就整条跳过。 */
	@Nullable
	private static List<Object> toTomlList(Field field, List<?> list) {
		var result = new ArrayList<>(list.size());
		for (var element : list) {
			if (element == null) {
				warnNull(field, "list contains a null element");
				return null;
			}
			var converted = scalar(field, element);
			if (converted == null) return null;
			result.add(converted);
		}
		return result;
	}
	/** 标量转成 TOML 原生类型；枚举转成名字字符串。 */
	@Nullable
	private static Object scalar(Field field, Object value) {
		return switch (value) {
			case Boolean bool -> bool;
			case Integer intValue -> intValue;
			case Long longValue -> longValue;
			case Short shortValue -> shortValue;
			case Byte byteValue -> byteValue;
			case Double doubleValue -> finite(field, doubleValue);
			case Float floatValue -> finite(field, floatValue);
			case Enum<?> anEnum -> anEnum.name();
			case String string -> string;
			default -> warnNull(field, "unsupported type " + value.getClass().getName());
		};
	}
	/** 非有限浮点跳过并告警（NaN/Infinity 不是合法的 TOML 浮点）。 */
	@Nullable
	private static Double finite(Field field, double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			warnNull(field, "value is not a finite number");
			return null;
		}
		return value;
	}
	@Nullable
	private static Float finite(Field field, float value) {
		if (Float.isNaN(value) || Float.isInfinite(value)) {
			warnNull(field, "value is not a finite number");
			return null;
		}
		return value;
	}
	/** Point 写成子表，读侧仍按 x/y 取值。 */
	private static CommentedConfig pointConfig(Point point) {
		var config = inMemory();
		config.set("x", point.x);
		config.set("y", point.y);
		return config;
	}
	/** 记录跳过原因并返回 {@code null}，便于在表达式位置直接使用。 */
	@Nullable
	private static Object warnNull(Field field, String reason) {
		LOGGER.warn("Skipping option '{}': {}", field.getName(), reason);
		return null;
	}
	/** 字符串字段赋值（含长度校正）；字段不是 String 或长度低于下限无法修正时返回 {@code false}。 */
	private static boolean setString(Object category, Field field, String value) throws IllegalAccessException {
		if (field.getType() != String.class) return false;
		var corrected = FieldValidators.correct(field, value);
		if (corrected == null) return false;
		field.set(category, corrected);
		return true;
	}
	/** 把 TOML 数组转成字段声明的元素类型；出现不支持的元素时整条放弃。 */
	@Nullable
	private static List<Object> toList(Field field, List<?> raw) {
		var elementType = FieldAccess.elementType(field);
		if (elementType == null) {
			LOGGER.warn("Skipping option '{}': raw List without an element type is not supported", field.getName());
			return null;
		}
		var result = new ArrayList<>(raw.size());
		for (var element : raw) {
			var converted = toElement(field, elementType, element);
			if (converted == null) return null;
			result.add(converted);
		}
		return result;
	}
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Nullable
	private static Object toElement(Field field, Class<?> elementType, @Nullable Object element) {
		if (element == null) return warnNull(field, "list contains a null element");
		if (elementType.isEnum()) {
			if (!(element instanceof String name)) return warnNull(field, "list element is not an enum name");
			return Enum.valueOf((Class) elementType, name);
		}
		switch (element) {
			case Number num -> {
				if (elementType == Integer.class) return num.intValue();
				if (elementType == Long.class) return num.longValue();
				if (elementType == Double.class) return num.doubleValue();
				if (elementType == Float.class) return num.floatValue();
				if (elementType == Short.class) return num.shortValue();
				if (elementType == Byte.class) return num.byteValue();
				if (elementType == String.class) return num.toString();
				return warnNull(field, "unsupported list element type " + elementType.getSimpleName());
			}
			case Boolean bool when elementType == Boolean.class -> {
				return bool;
			}
			case String string when elementType == String.class -> {
				return string;
			}
			default -> {}
		}
		return warnNull(field, "list element does not match " + elementType.getSimpleName());
	}
	/** 设置读写目标文件；由 {@link ConfigHandler#loadFromDisk} 在路径解析后调用。 */
	public void setTargetPath(Path targetPath) {
		this.targetPath = targetPath;
	}
	private Path targetPath() {
		if (targetPath == null) throw new IllegalStateException("Config target path has not been resolved yet");
		return targetPath;
	}
	/** 原子写入：先写临时文件再原子替换，写入失败时原文件保持完整。 */
	public void serialize(C config, Map<String, String> keybinds) throws SerializationException {
		try {
			var toml = toConfig(config);
			writeKeybinds(toml, keybinds);
			new TomlWriter().write(toml, targetPath(), WritingMode.REPLACE_ATOMIC);
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	/** 把配置写成 TOML 文本（不含快捷键段）；供网络下发等场景。 */
	public String writeToString(C config) throws SerializationException {
		try {
			return new TomlWriter().writeToString(toConfig(config));
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	/** 读取目标文件；文件不存在返回 {@code null}。 */
	public @Nullable CommentedConfig read() throws SerializationException {
		return read(targetPath());
	}
	/** 读取指定文件；文件不存在返回 {@code null}。 */
	public static @Nullable CommentedConfig read(Path path) throws SerializationException {
		if (!Files.exists(path)) return null;
		try (var reader = Files.newBufferedReader(path)) {
			var toml = inMemory();
			new TomlParser().parse(reader, toml, ParsingMode.REPLACE);
			return toml;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	/** 构建整份配置的 TOML 表示（值 + 本地化注释）；不写盘。 */
	private CommentedConfig toConfig(C config) throws SerializationException {
		var toml = inMemory();
		try {
			for (var categoryField : getCategoryFields()) {
				categoryField.setAccessible(true);
				writeCategory(toml, categoryField.get(config), categoryField.getName());
			}
		} catch (Exception e) {
			throw new SerializationException(e);
		}
		return toml;
	}
	/** 快捷键段：键是配置路径（含点号），必须按字面键写入，否则点号会被解析成层级。 */
	private static void writeKeybinds(CommentedConfig toml, Map<String, String> keybinds) {
		if (keybinds.isEmpty()) return;
		var section = toml.createSubConfig();
		keybinds.forEach((path, value) -> section.set(List.of(path), value));
		toml.set(KEYBINDS_SECTION, section);
	}
	/** 读取快捷键段；兼容旧的、点号被解析成层级的写法。 */
	public static Map<String, String> readKeybinds(@Nullable CommentedConfig toml) {
		var result = new LinkedHashMap<String, String>();
		Object raw = toml == null ? null : toml.get(KEYBINDS_SECTION);
		if (raw instanceof UnmodifiableConfig section) flatten(section, "", result);
		return result;
	}
	private static void flatten(UnmodifiableConfig config, String prefix, Map<String, String> out) {
		for (var entry : config.entrySet()) {
			var path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
			var value = entry.getValue();
			if (value instanceof UnmodifiableConfig sub) flatten(sub, path, out);
			else if (value instanceof String string) out.put(path, string);
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
		for (var field : FieldAccess.collectOrderedFields(configClass))
			if (field.isAnnotationPresent(Category.class)) list.add(field);
		return list.toArray(new Field[0]);
	}
	private C newInstance() throws SerializationException {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new SerializationException(e);
		}
	}
	/** 递归写出一个分类子表；{@code path} 是用于翻译键的完整点分路径。 */
	private void writeCategory(CommentedConfig parent, Object category, String path) throws IllegalAccessException {
		var name = path.substring(path.lastIndexOf('.') + 1);
		var comment = translate("category." + path);
		if (comment != null) parent.setComment(List.of(name), " " + comment.trim());
		var section = parent.createSubConfig();
		parent.set(name, section);
		for (var field : FieldAccess.collectOrderedFields(category.getClass())) {
			field.setAccessible(true);
			var fieldName = field.getName();
			if (field.isAnnotationPresent(Category.class)) {
				writeCategory(section, field.get(category), "%s.%s".formatted(path, fieldName));
				continue;
			}
			// 先算值：不支持的值直接跳过，避免留下悬空注释
			var value = toTomlValue(field, field.get(category));
			if (value == null) continue;
			var fieldComment = buildFieldComment(path, fieldName);
			if (!fieldComment.isEmpty()) section.setComment(List.of(fieldName), fieldComment);
			section.set(fieldName, value);
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
		// Translation 依赖客户端 I18n，专用服务端触碰会加载 client-only 类；服务端不生成注释即可
		if (translationPrefix == null || FMLEnvironment.dist.isDedicatedServer()) return null;
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
	private void readCategory(CommentedConfig config, String categoryName, Object category, Map<String, Object> fallbackValues) throws
		IllegalAccessException {
		CommentedConfig subConfig = config.get(categoryName) instanceof CommentedConfig cc ? cc : null;
		for (var field : FieldAccess.collectOrderedFields(category.getClass())) {
			field.setAccessible(true);
			if (field.isAnnotationPresent(Category.class)) {
				readCategory(subConfig != null ? subConfig : config, field.getName(), field.get(category), fallbackValues);
				continue;
			}
			Object value = subConfig != null ? subConfig.get(field.getName()) : null;
			if (value == null && fallbackValues != null) value = fallbackValues.get(field.getName());
			if (value == null) continue;
			// 单条非法或类型不符只跳过该条并保留默认值，绝不让一个坏值把整份配置拖回默认
			try {
				if (!applyValue(category, field, value)) LOGGER.warn(
					"Ignoring option '{}': value '{}' does not match type {}",
					field.getName(),
					value,
					field.getType().getSimpleName()
				);
			} catch (Exception e) {
				LOGGER.warn("Ignoring option '{}': failed to read value '{}'", field.getName(), value, e);
			}
		}
	}
	/** 按字段类型转换文件中的原始值并写入（含范围校正）；类型不匹配或不受支持时返回 {@code false}，由调用方保留默认值。 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private boolean applyValue(Object category, Field field, Object value) throws IllegalAccessException {
		var type = field.getType();
		if (type == Point.class) {
			if (!(value instanceof CommentedConfig cc)) return false;
			field.set(category, new Point(cc.getInt("x"), cc.getInt("y")));
			return true;
		}
		if (type.isEnum()) {
			if (!(value instanceof String name)) return false;
			field.set(category, Enum.valueOf((Class) type, name));
			return true;
		}
		if (value instanceof Number num) {
			if (type == int.class || type == Integer.class) field.setInt(category, FieldValidators.correct(field, num.intValue()));
			else if (type == long.class || type == Long.class) field.setLong(category, FieldValidators.correct(field, num.longValue()));
			else if (type == double.class || type == Double.class)
				field.setDouble(category, FieldValidators.correct(field, num.doubleValue()));
			else if (type == float.class || type == Float.class) field.setFloat(category, FieldValidators.correct(field,
				num.floatValue()));
			else if (type == byte.class || type == Byte.class) field.setByte(category, num.byteValue());
			else if (type == short.class || type == Short.class) field.setShort(category, num.shortValue());
			else return setString(category, field, num.toString());
			return true;
		}
		if (List.class.isAssignableFrom(type)) {
			if (!(value instanceof List<?> raw)) return false;
			var list = toList(field, raw);
			if (list == null) return false;
			field.set(category, list);
			return true;
		}
		// 原始 boolean 的 isInstance 恒为 false，需单独放行
		if (value instanceof Boolean bool && (type == boolean.class || type == Boolean.class)) {
			field.set(category, bool);
			return true;
		}
		if (value instanceof String string) return setString(category, field, string);
		if (!type.isInstance(value)) return false;
		field.set(category, value);
		return true;
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
