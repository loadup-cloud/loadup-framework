package io.github.loadup.gateway.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a Spring service method as callable by a managed gateway route. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GatewayExpose {
    /** Stable identifier for an overloaded method. Empty means its Java name. */
    String value() default "";
}
