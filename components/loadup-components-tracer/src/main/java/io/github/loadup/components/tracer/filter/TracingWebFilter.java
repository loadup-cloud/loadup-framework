package io.github.loadup.components.tracer.filter;

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

import io.github.loadup.components.tracer.config.TracerProperties;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
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

/** Web filter for automatic HTTP request tracing. */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
        prefix = "loadup.tracer",
        name = "enable-web-tracing",
        havingValue = "true",
        matchIfMissing = true)
public class TracingWebFilter extends OncePerRequestFilter {

    private final Tracer tracer;
    private final TextMapPropagator textMapPropagator;
    private final TracerProperties tracerProperties;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!tracerProperties.isEnabled() || shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract context from incoming request headers
        Context extractedContext =
                textMapPropagator.extract(Context.current(), request, new HttpServletRequestGetter());

        String spanName = request.getMethod() + " " + request.getRequestURI();
        Span span = tracer.spanBuilder(spanName).setParent(extractedContext).startSpan();

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
                Collections.list(request.getHeaderNames())
                        .forEach(headerName ->
                                span.setAttribute("http.header." + headerName, request.getHeader(headerName)));
            }

            if (tracerProperties.isIncludeParameters()) {
                request.getParameterMap()
                        .forEach((key, values) -> span.setAttribute("http.param." + key, String.join(",", values)));
            }

            // Inject trace context into response headers
            textMapPropagator.inject(
                    Context.current(), response, (carrier, key, value) -> carrier.setHeader(key, value));

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
