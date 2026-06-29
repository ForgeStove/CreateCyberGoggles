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

	/**
	 * Whether the category should be expanded by default in the config GUI.
	 * Subcategories can be collapsed/expanded by clicking on the header.
	 * <p>
	 * Default value is true.
	 *
	 * @return whether the category is expanded by default
	 */
	boolean defaultExpanded() default true;
}
