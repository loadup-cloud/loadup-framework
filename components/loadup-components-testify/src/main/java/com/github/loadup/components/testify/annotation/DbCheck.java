package com.github.loadup.components.testify.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 用于在测试方法执行前后校验数据库数据
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DbCheck {
    /**
     * 校验 SQL 文件路径（classpath 路径）
     */
    String value();

    /**
     * 是否在测试方法执行前校验（默认 false）
     */
    boolean before() default false;

    /**
     * 是否在测试方法执行后校验（默认 true）
     */
    boolean after() default true;
}
