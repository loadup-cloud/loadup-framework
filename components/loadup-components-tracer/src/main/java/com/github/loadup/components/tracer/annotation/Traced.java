package com.github.loadup.components.tracer.annotation;
import java.lang.annotation.*;
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Traced {
    String name() default "";
    String[] attributes() default {};
    boolean includeParameters() default false;
    boolean includeResult() default false;
}
