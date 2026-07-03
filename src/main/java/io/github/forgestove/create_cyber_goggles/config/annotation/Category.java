package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
/**
 * 标记包含分类POJO的字段。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
	/**
	 * 分类在配置GUI中是否默认展开。
	 * 子分类可以通过点击标题折叠/展开。
	 * <p>
	 * 默认值为 true。
	 *
	 * @return 分类是否默认展开
	 */
	boolean value() default true;
}
