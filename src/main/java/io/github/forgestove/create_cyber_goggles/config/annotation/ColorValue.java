package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
/**
 * 将整数字段标记为颜色值。
 * 这将在配置GUI中显示颜色选择器。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColorValue {
	/**
	 * 颜色是否包含Alpha通道。
	 *
	 * @return 如果颜色包含Alpha通道（ARGB）则返回true，仅RGB则返回false
	 */
	boolean value() default false;
}
