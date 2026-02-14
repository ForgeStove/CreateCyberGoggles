package io.github.forgestove.create_cyber_goggles.config.annotation;
import java.lang.annotation.*;
/**
 * Notifies the user that the game needs to be restarted for a change to the field to take effect.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRestart {}

