package io.github.forgestove.create_cyber_goggles.mixin;
import io.github.forgestove.create_cyber_goggles.foundation.util.CCGMods;

import java.lang.annotation.*;
/**
 * 根据以下条件控制 Mixin 是否应用：
 * <p>{@link #value()} 中的模组全部已安装</p>
 * <p>{@link #absent()} 中的模组全部未安装</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModCondition {
	CCGMods[] value() default {};
	CCGMods[] absent() default {};
}
