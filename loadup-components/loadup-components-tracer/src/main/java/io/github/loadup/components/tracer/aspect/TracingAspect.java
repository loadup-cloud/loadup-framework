package io.github.loadup.components.tracer.aspect;

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

import io.github.loadup.components.tracer.TraceContext;
import io.github.loadup.components.tracer.TraceUtil;
import io.github.loadup.components.tracer.annotation.Traced;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * AOP aspect that wraps methods annotated with {@link Traced} in an OpenTelemetry span.
 *
 * <p>Behavior:
 * <ul>
 *   <li>Span name: annotation {@code name} attribute, or {@code ClassName.methodName}.</li>
 *   <li>Parameters / return value are recorded when their flag is {@code true}.</li>
 *   <li>Exceptions are recorded on the span and then re-thrown – never swallowed.</li>
 *   <li>MDC ({@code traceId}, {@code spanId}) is restored to the parent values on exit.</li>
 * </ul>
 */
@Aspect
public class TracingAspect {
    private static final Logger log = LoggerFactory.getLogger(TracingAspect.class);


    @Around("@annotation(io.github.loadup.components.tracer.annotation.Traced) "
            + "|| @within(io.github.loadup.components.tracer.annotation.Traced)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();

        // Resolve @Traced from method first, then fall back to class level.
        Traced traced = signature.getMethod().getAnnotation(Traced.class);
        if (traced == null) {
            traced = AnnotationUtils.findAnnotation(pjp.getTarget().getClass(), Traced.class);
        }

        String spanName = resolveSpanName(traced, pjp);

        // Save outer MDC values so we can restore them after the span ends.
        String outerTraceId = org.slf4j.MDC.get(TraceUtil.MDC_TRACE_ID);
        String outerSpanId = org.slf4j.MDC.get(TraceUtil.MDC_SPAN_ID);

        Span span = TraceUtil.getTracer().spanBuilder(spanName).startSpan();
        TraceContext context = TraceUtil.getTraceContext();
        context.push(span);
        TraceUtil.injectMdc(span);

        try (Scope scope = span.makeCurrent()) {
            if (traced != null && traced.includeParameters() && pjp.getArgs() != null) {
                span.setAttribute(AttributeKey.stringKey("method.parameters"), Arrays.toString(pjp.getArgs()));
            }

            Object result = pjp.proceed();

            if (traced != null && traced.includeResult() && result != null) {
                span.setAttribute(AttributeKey.stringKey("method.result"), result.toString());
            }
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR, t.getMessage());
            span.recordException(t);
            throw t;
        } finally {
            context.pop();
            span.end();
            restoreMdc(outerTraceId, outerSpanId);
        }
    }

    private static String resolveSpanName(Traced traced, ProceedingJoinPoint pjp) {
        if (traced != null && !traced.name().isEmpty()) {
            return traced.name();
        }
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        return className + "." + methodName;
    }

    private static void restoreMdc(String outerTraceId, String outerSpanId) {
        if (outerTraceId != null) {
            org.slf4j.MDC.put(TraceUtil.MDC_TRACE_ID, outerTraceId);
        } else {
            TraceUtil.clearMdc();
        }
        if (outerSpanId != null) {
            org.slf4j.MDC.put(TraceUtil.MDC_SPAN_ID, outerSpanId);
        }
    }
}
