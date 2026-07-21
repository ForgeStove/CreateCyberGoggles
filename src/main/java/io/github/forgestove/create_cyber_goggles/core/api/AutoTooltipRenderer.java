package io.github.forgestove.create_cyber_goggles.core.api;
import io.github.forgestove.create_cyber_goggles.core.event.ItemTooltip;

import java.lang.annotation.*;
/** 标记 {@link TooltipRenderer} 实现类，使得 {@link ItemTooltip} 通过 NeoForge ASM 扫描自动发现并注册 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoTooltipRenderer {}
