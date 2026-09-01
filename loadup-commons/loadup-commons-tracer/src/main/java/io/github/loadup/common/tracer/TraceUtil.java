package io.github.loadup.common.tracer;

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

import io.github.loadup.commons.log.LogContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;

/**
 * Static facade for the OpenTelemetry tracer.
 *
 * <p>Initialized as a Spring bean so that the underlying OTel objects are available
 * via static methods throughout the application (e.g. in non-Spring-managed classes).
 *
 * <p>MDC keys injected: {@code traceId}, {@code spanId}.
 */
public class TraceUtil {

    public static final String MDC_TRACE_ID = LogContext.TRACE_ID;
    public static final String MDC_SPAN_ID = LogContext.SPAN_ID;

    private static final TraceContext TRACE_CONTEXT = new TraceContext();

    private static volatile Tracer staticTracer;
    private static volatile String staticAppName = "unknown-service";

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    private final String applicationName;

    @PostConstruct
    void init() {
        initialize(tracer, applicationName);
    }

    /**
     * Publishes the tracer and application name to the static facade. Only called from the
     * Spring bean lifecycle; kept static so SpotBugs does not flag static writes from an
     * instance method.
     */
    static void initialize(Tracer tracer, String applicationName) {
        staticTracer = tracer;
        staticAppName = applicationName;
    }

    // -------------------------------------------------------------------------
    // Static API
    // -------------------------------------------------------------------------

    /**
     * Returns the active {@link Tracer}.
     *
     * @return the tracer
     */
    public static Tracer getTracer() {
        return staticTracer;
    }

    /**
     * Returns the application name resolved from {@code spring.application.name}.
     */
    public static String getApplicationName() {
        return staticAppName;
    }

    /**
     * Starts a new span, pushes it onto the {@link TraceContext} stack, and injects
     * {@code traceId} / {@code spanId} into SLF4J MDC for structured log correlation.
     *
     * <p>The caller is responsible for ending the span via {@code span.end()}.
     * MDC values are NOT cleared here; use {@link #clearMdc()} in the finally block
     * when you own the full request lifecycle (see {@code TracingWebFilter}).
     *
     * @param operationName the span name
     * @return the started span
     */
    public static Span createSpan(String operationName) {
        Span span = staticTracer.spanBuilder(operationName).startSpan();
        pushSpan(span);
        injectMdc(span);
        return span;
    }

    /**
     * Returns the span currently at the top of the context stack, or {@code null}.
     *
     * @return the current span
     */
    public static Span getSpan() {
        return TRACE_CONTEXT.getCurrentSpan();
    }

    /**
     * Pushes a span onto the thread-local context stack.
     *
     * @param span the span to push
     */
    public static void pushSpan(Span span) {
        TRACE_CONTEXT.push(span);
    }

    /**
     * Pops the current span from the thread-local context stack.
     *
     * @return the popped span, or {@code null} when the stack is empty
     */
    public static Span popSpan() {
        return TRACE_CONTEXT.pop();
    }

    /**
     * Clears the thread-local context stack for the current thread.
     */
    public static void clearContext() {
        TRACE_CONTEXT.clear();
    }

    /**
     * Returns the trace ID of the current span as a 32-hex-character string.
     * Falls back to the OTel current-span API when the local stack is empty.
     *
     * @return the trace ID, or an all-zeros string when no span is active
     */
    public static String getTracerId() {
        Span span = TRACE_CONTEXT.getCurrentSpan();
        if (span == null) {
            span = Span.current();
        }
        return span.getSpanContext().getTraceId();
    }

    // -------------------------------------------------------------------------
    // MDC helpers (package-private, used by TracingAspect and TracingWebFilter)
    // -------------------------------------------------------------------------

    /**
     * Injects trace context into SLF4J MDC.
     */
    public static void injectMdc(Span span) {
        if (span != null && span.getSpanContext().isValid()) {
            LogContext.putTraceContext(span);
        }
    }

    /**
     * Clears tracer-injected MDC keys.
     */
    public static void clearMdc() {
        LogContext.clearTraceContext();
    }

    /**
     * Alias for {@link #injectMdc(Span)} — injects span context into MDC.
     *
     * @param span the active span
     */
    public static void logTraceId(Span span) {
        injectMdc(span);
    }

    /**
     * Alias for manual MDC injection with a custom trace ID string.
     *
     * @param traceId the trace ID to inject
     */
    public static void logTraceId(String traceId) {
        if (traceId != null) {
            MDC.put(MDC_TRACE_ID, traceId);
        }
    }

    /**
     * Alias for {@link #clearMdc()} — removes tracer-injected MDC keys.
     */
    public static void clearTraceId() {
        clearMdc();
    }

    public TraceUtil(OpenTelemetry openTelemetry, Tracer tracer) {
        this.openTelemetry = openTelemetry;
        this.tracer = tracer;
        this.applicationName = "";
    }

    public TraceUtil(OpenTelemetry openTelemetry, Tracer tracer, String applicationName) {
        this.openTelemetry = openTelemetry;
        this.tracer = tracer;
        this.applicationName = applicationName;
    }
}
