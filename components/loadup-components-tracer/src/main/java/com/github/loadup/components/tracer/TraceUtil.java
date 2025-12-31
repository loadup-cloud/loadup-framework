package com.github.loadup.components.tracer;

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
