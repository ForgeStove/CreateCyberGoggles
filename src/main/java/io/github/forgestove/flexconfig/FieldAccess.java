package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.api.Order;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
public final class FieldAccess {
	/**
	 * 收集类自身声明的字段并按 {@link Order @Order} 排序。
	 * 无注解者视为 0 排在前面并保持声明顺序；有正数值者按值排序再按声明顺序。
	 * <p>
	 * 配置树与序列化器必须共用此方法，否则「GUI 能看到但永不落盘」一类不一致会再次出现。
	 */
	public static Field[] collectOrderedFields(Class<?> clazz) {
		var fields = clazz.getDeclaredFields();
		var indices = new HashMap<String, Integer>();
		for (var i = 0; i < fields.length; i++) indices.put(fields[i].getName(), i);
		Arrays.sort(
			fields, Comparator.comparingInt((Field f) -> {
				var order = f.getAnnotation(Order.class);
				return order != null ? order.value() : 0;
			}).thenComparingInt(f -> indices.get(f.getName()))
		);
		return fields;
	}
	/** 原始类型映射到包装类型（值类型注册表按包装类型索引），其余原样返回。 */
	public static Class<?> boxed(Class<?> type) {
		if (type == int.class) return Integer.class;
		if (type == boolean.class) return Boolean.class;
		if (type == long.class) return Long.class;
		if (type == float.class) return Float.class;
		if (type == double.class) return Double.class;
		if (type == byte.class) return Byte.class;
		if (type == short.class) return Short.class;
		if (type == char.class) return Character.class;
		return type;
	}
	/** 取 {@code List<T>} 声明的元素类型；裸 {@code List} 或非 Class 实参时返回 {@code null}。 */
	public static Class<?> elementType(Field field) {
		if (field.getGenericType() instanceof ParameterizedType parameterized
			&& parameterized.getActualTypeArguments()[0] instanceof Class<?> element) return boxed(element);
		return null;
	}
	public static VarHandle varHandle(Field field) {
		try {
			return MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectVarHandle(field);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Cannot access field: " + field.getName(), e);
		}
	}
	public static Object getFieldValue(Field field, Object target) {
		try {
			field.setAccessible(true);
			return field.get(target);
		} catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
			throw new IllegalArgumentException("Failed to access field: " + field.getName(), e);
		}
	}
	public static Object readField(VarHandle handle, Object target, Field field) {
		try {
			return handle.get(target);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Failed to read " + field.getName(), e);
		}
	}
	public static void writeField(VarHandle handle, Object target, Object value, Field field) {
		try {
			handle.set(target, value);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Failed to write " + field.getName(), e);
		}
	}
}
