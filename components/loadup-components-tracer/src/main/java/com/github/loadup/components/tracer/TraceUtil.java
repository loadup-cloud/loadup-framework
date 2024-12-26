package com.github.loadup.components.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TraceUtil {

    @Value("${spring.application.name:''}")
    private       String applicationName;
    private final Tracer tracer;

    private static TraceUtil traceUtil;
    private static String    staticApplicationName;

    @PostConstruct
    public void initialize() {
        traceUtil = this;
        staticApplicationName = applicationName;
    }

    public static Tracer getTracer() {
        return traceUtil.tracer;
    }

    public static Span getSpan() {
        TraceContext sofaTraceContext = TraceContextHolder.getSofaTraceContext();
        return sofaTraceContext.getCurrentSpan();
    }

    public static Span createSpan(String moduleType) {
        Span span = getTracer().spanBuilder(moduleType).startSpan();
        TraceContextHolder.getSofaTraceContext().push(span);
        return span;
    }

    public static Span createSpan(String moduleType, Context parentContext) {
        Span span = getTracer().spanBuilder(moduleType).setParent(parentContext).startSpan();
        TraceContextHolder.getSofaTraceContext().push(span);
        return span;
    }

    public static String getTracerId() {
        return getSpan().getSpanContext().getTraceId();
    }

    public static void logTraceId(Span span) {
        //MDCUtil.logStartedSpan(span);
    }

    public static void clearTraceId() {
        //MDCUtil.logStoppedSpan();
    }

}
