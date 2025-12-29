/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.tracer;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TraceUtil {
    private static final TraceContext TRACE_CONTEXT = new TraceContext();

    private static TraceUtil instance;
    private final Tracer tracer;

    @Value("${spring.application.name:''}")
    private String applicationName;

    public static TraceContext getTraceContext() {
        return TRACE_CONTEXT;
    }

    public static TraceUtil getInstance() {
        return instance;
    }

    public static Tracer getTracer() {
        return instance.tracer;
    }

    public static String getApplicationName() {
        return instance.applicationName;
    }

    public static Span getSpan() {
        TraceContext traceContext = TraceUtil.getTraceContext();
        return traceContext.getCurrentSpan();
    }

    public static Span createSpan(String moduleType) {
        Span span = getTracer().spanBuilder(moduleType).startSpan();
        TraceUtil.getTraceContext().push(span);
        return span;
    }

    public static Span createSpan(String moduleType, Context parentContext) {
        Span span = getTracer().spanBuilder(moduleType).setParent(parentContext).startSpan();
        TraceUtil.getTraceContext().push(span);
        return span;
    }

    public static String getTracerId() {
        Span span = getSpan();
        if (span == null) {
            return Span.current().getSpanContext().getTraceId();
        }
        return span.getSpanContext().getTraceId();
    }

    public static void logTraceId(Span span) {
        MDC.put("traceId", span.getSpanContext().getTraceId());
    }

    public static void logTraceId(String traceId) {
        MDC.put("traceId", traceId);
    }

    public static void clearTraceId() {
        MDC.remove("traceId");
    }

    @PostConstruct
    public void initialize() {
        instance = this;
    }
}
