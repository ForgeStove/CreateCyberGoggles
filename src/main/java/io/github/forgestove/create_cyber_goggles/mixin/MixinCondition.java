package io.github.forgestove.create_cyber_goggles.mixin;
import io.github.forgestove.create_cyber_goggles.foundation.util.Mods;

import java.lang.annotation.*;
/**
 * 当 Mixin 满足以下条件时应用：
 * <p>
 * {@link #require()} 中的模组全部已安装
 * <p>
 * {@link #conflict()} 中的模组全部未安装
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MixinCondition {
	Mods[] require() default {};
	Mods[] conflict() default {};
}
