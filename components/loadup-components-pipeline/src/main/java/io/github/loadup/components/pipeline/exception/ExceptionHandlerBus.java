package io.github.loadup.components.pipeline.exception;

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

    /** Exception class → handler class mapping. */
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

    /** Creates a new fluent builder. */
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
