package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleRange {
	double min() default -Double.MAX_VALUE;
	double max() default Double.MAX_VALUE;
}
