package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LongRange {
    long min() default Long.MIN_VALUE;
    long max() default Long.MAX_VALUE;
}
