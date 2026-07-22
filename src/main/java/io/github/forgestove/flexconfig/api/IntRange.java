package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntRange {
	int min() default Integer.MIN_VALUE;
	int max() default Integer.MAX_VALUE;
}
