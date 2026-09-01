package io.github.loadup.common.tracer.annotation;

/*-
 * #%L
 * Loadup Common Tracer
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
 * Marks a method (or all public methods of a class) to be wrapped in an OpenTelemetry span.
 *
 * <p>The span name defaults to {@code ClassName.methodName} when {@link #name()} is not set.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Traced {

    /**
     * Explicit span name. When empty, {@code ClassName.methodName} is used.
     *
     * @return the span name
     */
    String name() default "";

    /**
     * When {@code true}, method parameters are recorded as span attributes.
     *
     * @return whether to include method parameters
     */
    boolean includeParameters() default false;

    /**
     * When {@code true}, the return value is recorded as a span attribute.
     *
     * @return whether to include the return value
     */
    boolean includeResult() default false;
}
