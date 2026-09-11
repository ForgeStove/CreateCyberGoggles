package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/**
 * 标记「该字段的值被改掉之后要执行副作用」，值确实变化时框架会调用 {@link ConfigChangeHandler}。
 * <p>
 * 触发时机是<b>保存时</b>（配置界面的保存按钮 / 快捷键落盘），不是逐字符编辑时；
 * 从磁盘加载（包括启动、服务端锁值下发）不会触发。
 * <p>
 * 与 {@link RequiresRestart} <b>互斥</b>：标了本注解说明改完即生效，不要再标 {@code @RequiresRestart}，
 * 否则保存时仍会弹重启确认。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnChange {
	/** 值变化后调用的处理器，需有无参构造 */
	Class<? extends ConfigChangeHandler> value();
}
