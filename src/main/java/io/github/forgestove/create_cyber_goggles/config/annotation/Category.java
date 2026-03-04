package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
/**
 * Denotes a field containing a category POJO.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
	/**
	 * Category order. Categories will be sorted by this value in ascending order.
	 * <p>
	 * Default value is 0.
	 *
	 * @return the order of the category
	 */
	int value() default 0;
}
