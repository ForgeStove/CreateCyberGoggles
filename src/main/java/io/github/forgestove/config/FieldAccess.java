package io.github.forgestove.config;
import java.lang.invoke.*;
import java.lang.reflect.*;
public final class FieldAccess {
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
