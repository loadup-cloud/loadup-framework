package io.github.loadup.components.extension.annotation;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2025 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个接口为扩展点
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Extension {
    /**
     * 扩展点的唯一标识，默认为接口全限定名
     */
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
