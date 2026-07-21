package io.github.loadup.components.extension.annotation;

/*-
 * #%L
 * Loadup Components Extension
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 {@code @Service} 方法参数或方法上，声明该方法需要绑定业务场景上下文。
 *
 * <p>与 {@link BizContextHolder} 和 {@code BizScenarioInterceptor} 配合使用：
 *
 * <ul>
 *   <li>标注在方法上：拦截器自动从参数中解析 {@code bizCode} 字段并写入上下文
 *   <li>标注在参数上：标记该参数为业务场景来源（参数类型必须实现 {@code BizIdentity} 接口）
 * </ul>
 *
 * <p>示例：
 *
 * <pre>{@code
 * @BizScenario
 * public OrderDTO createOrder(OrderCreateCommand cmd) { ... }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface BizScenario {

    /**
     * 指定固定的 bizCode，优先级高于运行时解析。
     *
     * <p>若不指定（空字符串），则拦截器从方法的第一个参数中读取 {@code bizCode} 字段。
     */
    String bizCode() default "";

    /**
     * 指定固定的 useCase，默认 "default"。
     */
    String useCase() default "default";

    /**
     * 指定固定的 scenario，默认 "default"。
     */
    String scenario() default "default";
}
