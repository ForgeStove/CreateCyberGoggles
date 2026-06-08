package io.github.forgestove.create_cyber_goggles.config;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode.*;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.*;
public final class FieldValidators {
	private static final Map<Class<?>, ValidatorFactory<?>> VALIDATOR_FACTORIES = Map.of(
		Integer.class,
		(ValidatorFactory<Integer>) valueField -> createRangeValidator(
			valueField,
			IntRange.class,
			r -> makeRangeValidator(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		Long.class,
		(ValidatorFactory<Long>) valueField -> createRangeValidator(
			valueField,
			LongRange.class,
			r -> makeRangeValidator(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		Float.class,
		(ValidatorFactory<Float>) valueField -> createRangeValidator(
			valueField,
			FloatRange.class,
			r -> makeRangeValidator(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		Double.class,
		(ValidatorFactory<Double>) valueField -> createRangeValidator(
			valueField,
			DoubleRange.class,
			r -> makeRangeValidator(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		String.class,
		(ValidatorFactory<String>) valueField -> createRangeValidator(
			valueField,
			StringLength.class,
			r -> makeRangeValidator(v -> v.length() < r.min(), v -> v.length() > r.max(), r.min(), r.max())
		)
	);
	@SuppressWarnings("unchecked")
	public static <V> @Nullable ValueValidator<V> validatorFor(Class<? extends V> type, Field valueField) {
		var factory = (ValidatorFactory<V>) VALIDATOR_FACTORIES.get(type);
		return factory == null ? null : factory.create(valueField);
	}
	private static <V, A extends Annotation> @Nullable ValueValidator<V> createRangeValidator(
		Field valueField,
		Class<A> annotationType,
		Function<A, ValueValidator<V>> validatorFactory
	) {
		var annotation = valueField.getAnnotation(annotationType);
		return annotation == null ? null : validatorFactory.apply(annotation);
	}
	private static <V> ValueValidator<V> makeRangeValidator(Predicate<V> belowMin, Predicate<V> aboveMax, Object min, Object max) {
		return value -> {
			if (belowMin.test(value)) return Translation.VALIDATOR_MIN.copy().append(String.valueOf(min));
			if (aboveMax.test(value)) return Translation.VALIDATOR_MAX.copy().append(String.valueOf(max));
			return null;
		};
	}
	@FunctionalInterface
	private interface ValidatorFactory<V> {
		ValueValidator<V> create(Field valueField);
	}
}
