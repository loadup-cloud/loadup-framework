/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.tracer.aop;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loadup.components.tracer.TraceUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ControllerTraceAspect {

    private final Tracer tracer;

    private final ObjectMapper objectMapper;

    private final TextMapPropagator textMapPropagator;

    @SneakyThrows
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object traceController(ProceedingJoinPoint joinPoint) {

        // 获取当前请求
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        Context parentContext = textMapPropagator.extract(Context.current(), request, getHeaderGetter());
        String spanName = request.getMethod() + ":" + request.getRequestURI();
        Span span = TraceUtil.createSpan(spanName, parentContext);
        //  创建Span
        Object result = null;
        try {
            span.setAttribute("method", request.getMethod());
            span.setAttribute("url", request.getRequestURI());

            log.info(
                    "start request:{}, spanId:{}, traceId:{},request:{}",
                    spanName,
                    span.getSpanContext().getSpanId(),
                    span.getSpanContext().getTraceId(),
                    objectMapper.writeValueAsString(joinPoint.getArgs()));
            result = joinPoint.proceed();
            log.info(
                    "response:{}, spanId:{}, traceId:{},result:{}",
                    spanName,
                    span.getSpanContext().getSpanId(),
                    TraceUtil.getTracerId(),
                    objectMapper.writeValueAsString(result));
            SpanContext spanContext = span.getSpanContext();
            if (spanContext.isValid()) {
                response.setHeader("traceId", spanContext.getTraceId());
            }
            Context context = span.storeInContext(parentContext);
            textMapPropagator.inject(context, response, (carrier, key, value) -> carrier.setHeader(key, value));
            return result;
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private TextMapGetter<HttpServletRequest> getHeaderGetter() {
        return new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(HttpServletRequest carrier) {
                if (carrier == null) {
                    return null;
                }
                return () -> carrier.getHeaderNames().asIterator();
            }

            @Override
            public String get(HttpServletRequest carrier, String key) {
                if (carrier == null) {
                    return null;
                }
                return carrier.getHeader(key);
            }
        };
    }
}
