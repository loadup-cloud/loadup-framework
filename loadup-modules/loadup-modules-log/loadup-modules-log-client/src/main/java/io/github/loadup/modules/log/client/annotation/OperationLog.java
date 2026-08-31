package io.github.loadup.modules.log.client.annotation;

/*-
 * #%L
 * Loadup Modules Log Client
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for automatic operation log recording via AOP.
 *
 * <p>Place on any Spring-managed bean method. The aspect will record the caller,
 * arguments, result, duration and success/failure asynchronously.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * Operation type: CREATE / UPDATE / DELETE / QUERY / EXPORT / LOGIN / LOGOUT
     */
    String type();

    /**
     * Business module name, e.g. "用户管理"
     */
    String module() default "";

    /**
     * Human-readable description, e.g. "删除用户"
     */
    String description() default "";

    /**
     * Whether to serialise method arguments into the log record
     */
    boolean recordParams() default false;

    /**
     * Whether to serialise the return value into the log record
     */
    boolean recordResponse() default false;
}
