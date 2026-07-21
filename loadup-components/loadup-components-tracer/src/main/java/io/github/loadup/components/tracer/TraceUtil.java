package io.github.loadup.components.tracer;

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

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

/**
 * Static facade for the OpenTelemetry tracer.
 *
 * <p>Initialized as a Spring bean so that the underlying OTel objects are available
 * via static methods throughout the application (e.g. in non-Spring-managed classes).
 *
 * <p>MDC keys injected: {@code traceId}, {@code spanId}.
 */
@RequiredArgsConstructor
public class TraceUtil {

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";

    private static final TraceContext TRACE_CONTEXT = new TraceContext();

    private static volatile Tracer staticTracer;
    private static volatile String staticAppName = "unknown-service";

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    @Value("${spring.application.name:unknown-service}")
    private String applicationName;

    @PostConstruct
    void init() {
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
     * Returns the shared {@link TraceContext} for the current thread group.
     *
     * @return the trace context
     */
    public static TraceContext getTraceContext() {
        return TRACE_CONTEXT;
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
        TRACE_CONTEXT.push(span);
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
            MDC.put(MDC_TRACE_ID, span.getSpanContext().getTraceId());
            MDC.put(MDC_SPAN_ID, span.getSpanContext().getSpanId());
        }
    }

    /**
     * Clears tracer-injected MDC keys.
     */
    public static void clearMdc() {
        MDC.remove(MDC_TRACE_ID);
        MDC.remove(MDC_SPAN_ID);
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
}
