package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/**
 * 配置分类或字段的条件加载。
 * <p>
 * 与 {@link Category} 一起应用于分类字段，或应用于分类POJO内的值字段。
 * 仅当所有指定条件都满足时，该元素才会被加载。
 * <p>
 * {@link #value()} 和 {@link #condition()} 之间是逻辑与关系。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {
	/**
	 * 快捷方式：仅当具有此ID的模组存在时加载。
	 * 留空以跳过模组检查。
	 *
	 * @return 模组ID，或空字符串表示无限制
	 */
	String value() default "";
	/**
	 * 要评估的条件类。每个类必须实现 {@link ConfigCondition}
	 * 并提供无参构造函数。
	 * <p>
	 * 如果全部返回 {@code true}，则包含该元素；否则
	 * 静默跳过。
	 *
	 * @return 条件类数组
	 */
	Class<? extends ConfigCondition>[] condition() default {};
}
