package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/**
 * 标记该字段的改动需要重新启动才能生效。
 * <p>
 * 保存时新值只写入配置文件，<b>运行时的值保持不变</b>，直到对应的重启发生：
 * <ul>
 * <li>{@link RestartType#WORLD}：退出世界（或服务端停止）时切换；</li>
 * <li>{@link RestartType#GAME}：本次游戏内不切换，重启游戏后自然读入新值。</li>
 * </ul>
 * 界面上按级别给出对应提示（返回标题界面 / 退出游戏）。
 * <p>
 * 与 {@link OnChange} <b>互斥</b>：标了 {@code @OnChange} 说明改完即生效，不要再标本注解。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRestart {
	/** 生效所需的动作；默认需要重启整个游戏。 */
	RestartType value() default RestartType.GAME;
}
