package io.github.loadup.components.pipeline.exception;

/*-
 * #%L
 * Loadup Components Pipeline
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registry of exception handlers for a single pipeline.
 *
 * <p>Usage (inside {@link io.github.loadup.components.pipeline.api.IPipelineDefinition#exceptions()}):
 * <pre>{@code
 * return ExceptionHandlerBus.builder()
 *     .register(BizException.class, OrderBizExceptionHandler.class)
 *     .overflow(UnrecoverableException.class)   // rethrow as-is
 *     .bottom(OrderSystemExceptionHandler.class); // catch-all fallback
 * }</pre>
 */
public final class ExceptionHandlerBus {

    /**
     * Exception class → handler class mapping.
     */
    private final Map<Class<? extends Throwable>, Class<? extends IExceptionClassHandler<?>>> exceptionHandlerMap =
            new HashMap<>();

    /**
     * Exception classes that should bypass all handlers and propagate to the caller.
     * Typically used for unrecoverable system errors.
     */
    private final Set<Class<? extends Throwable>> overflowExceptions = new HashSet<>();

    /**
     * Fallback handler invoked when no specific handler matches.
     * Must be set — the executor will throw {@link IllegalStateException} if it is missing
     * and an unhandled exception occurs.
     */
    private Class<? extends IExceptionClassHandler<?>> bottomHandler;

    private ExceptionHandlerBus() {}

    /**
     * Creates a new fluent builder.
     */
    public static ExceptionHandlerBus builder() {
        return new ExceptionHandlerBus();
    }

    // ── Fluent API ────────────────────────────────────────────────────────────

    /**
     * Register a handler for a specific exception class (exact match + sub-class matching).
     *
     * @param exceptionClass the exception class to match
     * @param handlerClass   the Spring-managed bean that handles it
     */
    public ExceptionHandlerBus register(
            Class<? extends Throwable> exceptionClass, Class<? extends IExceptionClassHandler<?>> handlerClass) {
        exceptionHandlerMap.put(exceptionClass, handlerClass);
        return this;
    }

    /**
     * Mark an exception class as an overflow — it will be re-thrown to the caller as-is
     * without invoking any handler.
     *
     * @param exceptionClass the exception class to re-throw
     */
    public ExceptionHandlerBus overflow(Class<? extends Throwable> exceptionClass) {
        overflowExceptions.add(exceptionClass);
        return this;
    }

    /**
     * Register the catch-all fallback handler.
     *
     * @param handlerClass the Spring-managed bean used when no specific handler matches
     */
    public ExceptionHandlerBus bottom(Class<? extends IExceptionClassHandler<?>> handlerClass) {
        this.bottomHandler = handlerClass;
        return this;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Map<Class<? extends Throwable>, Class<? extends IExceptionClassHandler<?>>> getExceptionHandlerMap() {
        return Collections.unmodifiableMap(exceptionHandlerMap);
    }

    public Set<Class<? extends Throwable>> getOverflowExceptions() {
        return Collections.unmodifiableSet(overflowExceptions);
    }

    public Class<? extends IExceptionClassHandler<?>> getBottomHandler() {
        return bottomHandler;
    }
}
