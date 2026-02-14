package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
/**
 * Denotes a field containing a category POJO.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigCategory {
	int ordinal() default 0;
}

