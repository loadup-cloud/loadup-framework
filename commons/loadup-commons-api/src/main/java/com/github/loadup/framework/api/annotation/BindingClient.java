package com.github.loadup.framework.api.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BindingClient {
  /** 对应 YAML 中的 bindings.key */
  String value() default "default";
}
