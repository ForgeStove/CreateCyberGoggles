package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LongRange {
	long min() default Long.MIN_VALUE;
	long max() default Long.MAX_VALUE;
}
