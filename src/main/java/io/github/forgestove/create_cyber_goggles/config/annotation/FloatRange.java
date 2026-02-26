package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FloatRange {
    float min() default -Float.MAX_VALUE;
    float max() default Float.MAX_VALUE;
}
