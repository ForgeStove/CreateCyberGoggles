package io.github.forgestove.create_cyber_goggles.config.annotation;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;

import java.lang.annotation.*;
/**
 * Specifies validation for a configuration field.
 * <p>
 * Can be used in two ways:
 * <ul>
 *   <li>Range validation: {@code @Range(min = 0, max = 100)}</li>
 *   <li>Custom validator: {@code @Range(validator = MyValidator.class)}</li>
 * </ul>
 * If both are specified, both validations will be applied.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
	/**
	 * Minimum value (inclusive).
	 */
	int min() default Integer.MIN_VALUE;
	/**
	 * Maximum value (inclusive).
	 */
	int max() default Integer.MAX_VALUE;
	/**
	 * Custom validator class. Use {@link ValueConfigNode.ValueValidator.None} for no custom validation.
	 */
	Class<? extends ValueConfigNode.ValueValidator<?>> validator() default ValueConfigNode.ValueValidator.None.class;
}
