package io.github.loadup.components.tracer.annotation;

/*-
 * #%L
 * Loadup Components Tracer
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
