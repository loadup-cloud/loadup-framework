/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.tracer.aspect;

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

import com.github.loadup.components.tracer.annotation.Traced;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect for automatic method tracing using @Traced annotation.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TracingAspect {

    private final Tracer tracer;

    @Around(
        "@annotation(com.github.loadup.components.tracer.annotation.Traced) || @within(com.github.loadup.components.tracer.annotation"
            + ".Traced)")
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Traced traced = method.getAnnotation(Traced.class);
        if (traced == null) {
            traced = joinPoint.getTarget().getClass().getAnnotation(Traced.class);
        }

        if (traced == null) {
            return joinPoint.proceed();
        }

        String spanName = getSpanName(traced, method);
        Span span = tracer.spanBuilder(spanName)
            .setParent(Context.current())
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Add custom attributes
            addCustomAttributes(span, traced);

            // Add method parameters if requested
            if (traced.includeParameters()) {
                addMethodParameters(span, signature, joinPoint.getArgs());
            }

            // Add class and method info
            span.setAttribute("code.namespace", joinPoint.getTarget().getClass().getName());
            span.setAttribute("code.function", method.getName());

            Object result = joinPoint.proceed();

            // Add return value if requested
            if (traced.includeResult() && result != null) {
                span.setAttribute("result.type", result.getClass().getSimpleName());
                span.setAttribute("result.value", result.toString());
            }

            span.setStatus(StatusCode.OK);
            return result;

        } catch (Throwable throwable) {
            span.recordException(throwable);
            span.setStatus(StatusCode.ERROR, throwable.getMessage());
            throw throwable;
        } finally {
            span.end();
        }
    }

    private String getSpanName(Traced traced, Method method) {
        if (traced.name() != null && !traced.name().isEmpty()) {
            return traced.name();
        }
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

    private void addCustomAttributes(Span span, Traced traced) {
        for (String attr : traced.attributes()) {
            String[] parts = attr.split("=", 2);
            if (parts.length == 2) {
                span.setAttribute(parts[0].trim(), parts[1].trim());
            }
        }
    }

    private void addMethodParameters(Span span, MethodSignature signature, Object[] args) {
        String[] parameterNames = signature.getParameterNames();
        if (parameterNames != null && args != null) {
            for (int i = 0; i < Math.min(parameterNames.length, args.length); i++) {
                if (args[i] != null) {
                    span.setAttribute("param." + parameterNames[i], args[i].toString());
                }
            }
        }
    }
}

