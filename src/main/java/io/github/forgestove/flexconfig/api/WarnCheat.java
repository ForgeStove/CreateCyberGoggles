package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/** 标记该配置项为作弊类功能，在配置界面的提示框下方追加一行红色警告。 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WarnCheat {}
