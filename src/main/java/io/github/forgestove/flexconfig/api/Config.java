package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	/** 模组 ID，同时用作翻译键前缀。 */
	String value();
	/** 配置类型，决定文件后缀、存放目录与加载时机。 */
	ConfigType type() default ConfigType.CLIENT;
	/** 显式文件名（可省略 {@code .toml}）；留空则按 {@code <modId>-<type>.toml} 推导。 */
	String fileName() default "";
}
