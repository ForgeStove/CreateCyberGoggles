package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IntRange {
	int min() default Integer.MIN_VALUE;
	int max() default Integer.MAX_VALUE;
}
