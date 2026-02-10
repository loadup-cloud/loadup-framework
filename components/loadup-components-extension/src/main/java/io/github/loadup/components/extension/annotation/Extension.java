package io.github.loadup.components.extension.annotation;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import java.lang.annotation.*;

/** 标识一个接口为扩展点 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Extension {
    /** 扩展点的唯一标识，默认为接口全限定名 */
    String bizCode() default "";

    /**
     * 用例 (Use Case) 在同一业务线下，区分不同的使用场景。默认为 "default"。
     *
     * @return 用例标识
     */
    String useCase() default "default";

    /**
     * 场景 (Scenario) 更细粒度的场景划分，作为第三匹配维度。默认为 "default"。
     *
     * @return 场景标识
     */
    String scenario() default "default";

    /**
     * 优先级 (Priority) 当多个扩展点同时满足匹配条件时，用于决策的优先级。数值越小，优先级越高。
     *
     * @return 优先级
     */
    int priority() default 0;
}
