/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.annotation;

import java.lang.annotation.*;

/**
 * 标识一个接口为扩展点
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Extension {
    /**
     * 扩展点的唯一标识，默认为接口全限定名
     */
    String bizCode() default "";
}
