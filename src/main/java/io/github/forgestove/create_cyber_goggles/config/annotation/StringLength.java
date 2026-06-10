package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringLength {
	int min() default 0;
	int max() default Integer.MAX_VALUE;
}
