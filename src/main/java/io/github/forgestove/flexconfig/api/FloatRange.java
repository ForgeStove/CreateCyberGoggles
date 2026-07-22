package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FloatRange {
	float min() default -Float.MAX_VALUE;
	float max() default Float.MAX_VALUE;
}
