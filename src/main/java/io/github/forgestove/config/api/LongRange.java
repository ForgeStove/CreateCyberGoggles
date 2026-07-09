package io.github.forgestove.config.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LongRange {
	long min() default Long.MIN_VALUE;
	long max() default Long.MAX_VALUE;
}
