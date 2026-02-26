package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DoubleRange {
    double min() default -Double.MAX_VALUE;
    double max() default Double.MAX_VALUE;
}
