package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringLength {
	int min() default 0;
	int max() default Integer.MAX_VALUE;
}
