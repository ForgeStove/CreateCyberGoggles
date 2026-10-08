package io.github.forgestove.flexconfig;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.tree.ValueConfigNode.ValueValidator;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.*;
public final class FieldValidators {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<Class<?>, ValidatorFactory<?>> VALIDATOR_FACTORIES = Map.of(
		Integer.class,
		(ValidatorFactory<Integer>) field -> validator(
			field,
			IntRange.class,
			r -> validate(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		Long.class,
		(ValidatorFactory<Long>) field -> validator(
			field,
			LongRange.class,
			r -> validate(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		Float.class,
		(ValidatorFactory<Float>) field -> validator(
			field,
			FloatRange.class,
			r -> validate(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		Double.class,
		(ValidatorFactory<Double>) field -> validator(
			field,
			DoubleRange.class,
			r -> validate(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		String.class,
		(ValidatorFactory<String>) field -> validator(
			field,
			StringLength.class,
			r -> validate(v -> v.length() < r.min(), v -> v.length() > r.max(), r.min(), r.max())
		)
	);
	@SuppressWarnings("unchecked")
	public static <V> @Nullable ValueValidator<V> validatorFor(Class<? extends V> type, Field valueField) {
		var factory = (ValidatorFactory<V>) VALIDATOR_FACTORIES.get(type);
		return factory == null ? null : factory.create(valueField);
	}
	private static <V, A extends Annotation> @Nullable ValueValidator<V> validator(
		Field valueField,
		Class<A> annotationType,
		Function<A, ValueValidator<V>> validatorFactory
	) {
		var annotation = valueField.getAnnotation(annotationType);
		return annotation == null ? null : validatorFactory.apply(annotation);
	}
	private static <V> ValueValidator<V> validate(Predicate<V> belowMin, Predicate<V> aboveMax, Number min, Number max) {
		return value -> {
			if (belowMin.test(value)) return Translation.VALIDATOR_MIN.copy().append(String.valueOf(min));
			if (aboveMax.test(value)) return Translation.VALIDATOR_MAX.copy().append(String.valueOf(max));
			return null;
		};
	}
	// ========== 加载期校正 ==========
	/** 按 Range 注解把越界数值夹到边界；无注解或未越界时原样返回。 */
	public static int correct(Field field, int value) {
		var range = field.getAnnotation(IntRange.class);
		if (range == null) return value;
		var corrected = Math.clamp(value, range.min(), range.max());
		if (corrected != value) corrected(field, value, corrected, range.min(), range.max());
		return corrected;
	}
	private static void corrected(Field field, Object from, Object to, Object min, Object max) {
		LOGGER.warn("Corrected '{}' from {} to {} (allowed {}..{})", field.getName(), from, to, min, max);
	}
	public static long correct(Field field, long value) {
		var range = field.getAnnotation(LongRange.class);
		if (range == null) return value;
		var corrected = Math.clamp(value, range.min(), range.max());
		if (corrected != value) corrected(field, value, corrected, range.min(), range.max());
		return corrected;
	}
	public static float correct(Field field, float value) {
		var range = field.getAnnotation(FloatRange.class);
		if (range == null || Float.isNaN(value)) return value;
		var corrected = Math.clamp(value, range.min(), range.max());
		if (corrected != value) corrected(field, value, corrected, range.min(), range.max());
		return corrected;
	}
	public static double correct(Field field, double value) {
		var range = field.getAnnotation(DoubleRange.class);
		if (range == null || Double.isNaN(value)) return value;
		var corrected = Math.clamp(value, range.min(), range.max());
		if (corrected != value) corrected(field, value, corrected, range.min(), range.max());
		return corrected;
	}
	/**
	 * 按 {@link StringLength} 把过长字符串截断到上限。
	 * 短于下限的字符串无法修正，返回 {@code null} 由调用方保留默认值。
	 */
	@Nullable
	public static String correct(Field field, String value) {
		var range = field.getAnnotation(StringLength.class);
		if (range == null) return value;
		if (value.length() > range.max()) {
			LOGGER.warn("Truncated '{}' to {} characters (allowed {}..{})", field.getName(), range.max(), range.min(), range.max());
			return value.substring(0, range.max());
		}
		if (value.length() < range.min()) {
			LOGGER.warn("Ignoring '{}': length {} is below the minimum {}", field.getName(), value.length(), range.min());
			return null;
		}
		return value;
	}
	@FunctionalInterface
	private interface ValidatorFactory<V> {
		ValueValidator<V> create(Field valueField);
	}
}
