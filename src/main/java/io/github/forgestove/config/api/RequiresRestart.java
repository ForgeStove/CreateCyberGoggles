package io.github.forgestove.config.api;
import java.lang.annotation.*;
/**
 * 通知用户更改该字段需要重新启动游戏才能生效。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRestart {}
