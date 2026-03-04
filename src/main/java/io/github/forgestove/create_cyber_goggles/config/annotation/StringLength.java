package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StringLength {
	int min() default 0;
	int max() default Integer.MAX_VALUE;
}
