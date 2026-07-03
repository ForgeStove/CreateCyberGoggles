package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	String value();
}
