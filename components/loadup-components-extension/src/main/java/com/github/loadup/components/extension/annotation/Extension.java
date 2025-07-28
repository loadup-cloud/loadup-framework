/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 标识一个接口为扩展点
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Component
public @interface Extension {
    /**
     * 扩展点的唯一标识，默认为接口全限定名
     */
    String bizCode() default "";

    /**
     * 用例 (Use Case)
     * 在同一业务线下，区分不同的使用场景。默认为 "default"。
     *
     * @return 用例标识
     */
    String useCase() default "default";

    /**
     * 场景 (Scenario)
     * 更细粒度的场景划分，作为第三匹配维度。默认为 "default"。
     *
     * @return 场景标识
     */
    String scenario() default "default";

    /**
     * 优先级 (Priority)
     * 当多个扩展点同时满足匹配条件时，用于决策的优先级。数值越小，优先级越高。
     *
     * @return 优先级
     */
    int priority() default 0;

}
