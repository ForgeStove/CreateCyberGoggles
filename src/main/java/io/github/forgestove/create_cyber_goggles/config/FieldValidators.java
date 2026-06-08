package io.github.forgestove.create_cyber_goggles.config;
import io.github.forgestove.create_cyber_goggles.config.annotation.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.*;
public final class FieldValidators {
	private static final Map<Class<?>, ValidatorFactory<?>> VALIDATOR_FACTORIES = Map.of(
		Integer.class,
		(ValidatorFactory<Integer>) (builder, valueField) -> applyRangeValidator(
			builder,
			valueField,
			IntRange.class,
			r -> makeRangeValidator(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		Long.class,
		(ValidatorFactory<Long>) (builder, valueField) -> applyRangeValidator(
			builder,
			valueField,
			LongRange.class,
			r -> makeRangeValidator(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		Float.class,
		(ValidatorFactory<Float>) (builder, valueField) -> applyRangeValidator(
			builder,
			valueField,
			FloatRange.class,
			r -> makeRangeValidator(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		Double.class,
		(ValidatorFactory<Double>) (builder, valueField) -> applyRangeValidator(
			builder,
			valueField,
			DoubleRange.class,
			r -> makeRangeValidator(v -> v < r.min(), v -> v > r.max(), r.min(), r.max())
		),
		String.class,
		(ValidatorFactory<String>) (builder, valueField) -> applyRangeValidator(
			builder,
			valueField,
			StringLength.class,
			r -> makeRangeValidator(v -> v.length() < r.min(), v -> v.length() > r.max(), r.min(), r.max())
		)
	);
	@SuppressWarnings("unchecked")
	public static <T> void apply(Builder<?, T, T> builder, Class<? extends T> type, Field valueField) {
		var factory = (ValidatorFactory<T>) VALIDATOR_FACTORIES.get(type);
		if (factory == null) return;
		factory.apply(builder, valueField);
	}
	private static <T, A extends Annotation> void applyRangeValidator(
		Builder<?, T, T> builder,
		Field valueField,
		Class<A> annotationType,
		Function<A, ValueValidator<T>> validatorFactory
	) {
		var annotation = valueField.getAnnotation(annotationType);
		if (annotation == null) return;
		builder.validator(validatorFactory.apply(annotation));
	}
	private static <T> ValueValidator<T> makeRangeValidator(Predicate<T> belowMin, Predicate<T> aboveMax, Object min, Object max) {
		return value -> {
			if (belowMin.test(value)) return Translation.VALIDATOR_MIN.copy().append(String.valueOf(min));
			if (aboveMax.test(value)) return Translation.VALIDATOR_MAX.copy().append(String.valueOf(max));
			return null;
		};
	}
	@FunctionalInterface
	private interface ValidatorFactory<T> {
		void apply(Builder<?, T, T> builder, Field valueField);
	}
}
