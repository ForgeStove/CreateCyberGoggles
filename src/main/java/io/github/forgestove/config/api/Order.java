package io.github.forgestove.config.api;
import java.lang.annotation.*;
/**
 * 指定字段在其声明类中序列化时的顺序。
 * 字段按此值升序排序。
 * <p>
 * 没有此注解的字段默认排序值为0，排在前面，
 * 保持来自 {@link Class#getDeclaredFields()} 的相对顺序。
 * <p>
 * 默认值为0。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
	int value() default 0;
}
