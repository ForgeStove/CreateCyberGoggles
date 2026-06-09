package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
/**
 * Conditional loading for config categories or fields.
 * <p>
 * Apply alongside {@link Category} on a category field, or on a value field
 * within a category POJO. The element is only loaded when all specified
 * conditions are met.
 * <p>
 * Both {@link #value()} and {@link #condition()} are AND-ed together.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {
	/**
	 * Shortcut: only load when the mod with this ID is present.
	 * Leave empty to skip the mod check.
	 *
	 * @return a mod ID, or empty string for no restriction
	 */
	String value() default "";
	/**
	 * Condition classes to evaluate. Each must implement {@link ConfigCondition}
	 * and provide a no-arg constructor.
	 * <p>
	 * If all return {@code true}, the element is included; otherwise
	 * it is silently skipped.
	 *
	 * @return array of condition classes
	 */
	Class<? extends ConfigCondition>[] condition() default {};
}
