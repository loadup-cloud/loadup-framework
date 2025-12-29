/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.tracer.filter;

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

import com.github.loadup.components.tracer.config.TracerProperties;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Web filter for automatic HTTP request tracing.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "loadup.tracer", name = "enable-web-tracing", havingValue = "true", matchIfMissing = true)
public class TracingWebFilter extends OncePerRequestFilter {

    private final Tracer            tracer;
    private final TextMapPropagator textMapPropagator;
    private final TracerProperties  tracerProperties;
    private final PathMatcher       pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!tracerProperties.isEnabled() || shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract context from incoming request headers
        Context extractedContext = textMapPropagator.extract(Context.current(), request,
            new HttpServletRequestGetter());

        String spanName = request.getMethod() + " " + request.getRequestURI();
        Span span = tracer.spanBuilder(spanName)
            .setParent(extractedContext)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Add HTTP attributes
            span.setAttribute("http.method", request.getMethod());
            span.setAttribute("http.url", request.getRequestURL().toString());
            span.setAttribute("http.scheme", request.getScheme());
            span.setAttribute("http.target", request.getRequestURI());

            if (request.getQueryString() != null) {
                span.setAttribute("http.query", request.getQueryString());
            }

            if (tracerProperties.isIncludeHeaders()) {
                Collections.list(request.getHeaderNames()).forEach(headerName ->
                    span.setAttribute("http.header." + headerName, request.getHeader(headerName))
                );
            }

            if (tracerProperties.isIncludeParameters()) {
                request.getParameterMap().forEach((key, values) ->
                    span.setAttribute("http.param." + key, String.join(",", values))
                );
            }

            // Inject trace context into response headers
            textMapPropagator.inject(Context.current(), response,
                (carrier, key, value) -> carrier.setHeader(key, value));

            filterChain.doFilter(request, response);

            // Add response status
            span.setAttribute("http.status_code", response.getStatus());

            if (response.getStatus() >= 400) {
                span.setStatus(StatusCode.ERROR);
            } else {
                span.setStatus(StatusCode.OK);
            }

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] patterns = tracerProperties.getExcludePatterns().split(",");

        for (String pattern : patterns) {
            if (pathMatcher.match(pattern.trim(), uri)) {
                return true;
            }
        }
        return false;
    }

    private static class HttpServletRequestGetter implements TextMapGetter<HttpServletRequest> {
        @Override
        public Iterable<String> keys(HttpServletRequest carrier) {
            return Collections.list(carrier.getHeaderNames());
        }

        @Override
        public String get(HttpServletRequest carrier, String key) {
            return carrier.getHeader(key);
        }
    }
}

