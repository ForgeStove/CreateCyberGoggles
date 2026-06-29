package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
/**
 * Specifies the order of a field within its declaring class during serialization.
 * Fields are sorted by this value in ascending order.
 * <p>
 * Fields without this annotation default to sort value 0 and come first,
 * preserving their relative order from {@link Class#getDeclaredFields()}.
 * <p>
 * Default value is 0.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
	int value() default 0;
}
