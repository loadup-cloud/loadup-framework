package com.github.loadup.components.tracer.aspect;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.github.loadup.components.tracer.annotation.Traced;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Aspect for automatic method tracing using @Traced annotation. */
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
    Span span = tracer.spanBuilder(spanName).setParent(Context.current()).startSpan();

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
