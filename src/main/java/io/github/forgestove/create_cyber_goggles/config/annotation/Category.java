package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
/**
 * Denotes a field containing a category POJO.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
	/**
	 * Whether the category should be expanded by default in the config GUI.
	 * Subcategories can be collapsed/expanded by clicking on the header.
	 * <p>
	 * Default value is true.
	 *
	 * @return whether the category is expanded by default
	 */
	boolean value() default true;
}
