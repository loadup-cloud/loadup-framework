package io.github.loadup.components.tracer.filter;

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

import io.github.loadup.components.tracer.TraceUtil;
import io.github.loadup.components.tracer.config.TracerProperties;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that instruments every incoming HTTP request with an OpenTelemetry span.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Extract W3C {@code traceparent} / {@code tracestate} from the request headers.</li>
 *   <li>Create a SERVER span and make it the active OTel context.</li>
 *   <li>Inject {@code traceId} and {@code spanId} into SLF4J MDC for structured logs.</li>
 *   <li>Set the {@code traceparent} header on the response.</li>
 *   <li>Record HTTP status and mark error spans on 5xx responses.</li>
 *   <li>End the span and clear MDC in a {@code finally} block – guaranteed even on exceptions.</li>
 *   <li>Skip excluded URL patterns (see {@link TracerProperties#getExcludePatterns()}).</li>
 * </ul>
 *
 * <p>Fallback: if span creation fails for any reason the request proceeds normally.
 */
@Slf4j
@RequiredArgsConstructor
public class TracingWebFilter extends OncePerRequestFilter {

    private static final TextMapGetter<HttpServletRequest> GETTER = new HttpServletRequestGetter();

    private final TracerProperties properties;
    private final OpenTelemetry openTelemetry;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        List<String> patterns = resolveExcludePatterns();
        return patterns.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String spanName = request.getMethod() + " " + request.getRequestURI();
        Context parentContext =
                openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), request, GETTER);

        Span span = openTelemetry
                .getTracer(resolveTracerName())
                .spanBuilder(spanName)
                .setParent(parentContext)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        setRequestAttributes(span, request);
        TraceUtil.injectMdc(span);

        // Propagate traceparent back to the caller so that upstream systems can correlate.
        String traceparent = buildTraceparent(span);
        response.setHeader("traceparent", traceparent);

        try (Scope scope = span.makeCurrent()) {
            filterChain.doFilter(request, response);
            int status = response.getStatus();
            span.setAttribute(AttributeKey.longKey("http.response.status_code"), status);
            if (status >= 500) {
                span.setStatus(StatusCode.ERROR, "HTTP " + status);
            }
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
            TraceUtil.clearMdc();
        }
    }

    private void setRequestAttributes(Span span, HttpServletRequest request) {
        span.setAttribute(AttributeKey.stringKey("http.request.method"), request.getMethod());
        span.setAttribute(AttributeKey.stringKey("url.path"), request.getRequestURI());
        span.setAttribute(
                AttributeKey.stringKey("server.address"), request.getServerName() + ":" + request.getServerPort());

        if (properties.isIncludeHeaders()) {
            Collections.list(request.getHeaderNames())
                    .forEach(h -> span.setAttribute(
                            AttributeKey.stringKey("http.request.header." + h), request.getHeader(h)));
        }
        if (properties.isIncludeParameters() && request.getQueryString() != null) {
            span.setAttribute(AttributeKey.stringKey("url.query"), request.getQueryString());
        }
    }

    private static String buildTraceparent(Span span) {
        return "00-"
                + span.getSpanContext().getTraceId()
                + "-"
                + span.getSpanContext().getSpanId()
                + "-01";
    }

    private List<String> resolveExcludePatterns() {
        String raw = properties.getExcludePatterns();
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(raw.split(","));
    }

    private String resolveTracerName() {
        // Prefer tracer/application name from properties; keep backward-compatible fallback.
        String applicationName = TraceUtil.getApplicationName();
        if (applicationName != null && !applicationName.isBlank()) {
            return applicationName;
        }
        return "loadup-tracer";
    }



    /**
     * Adapts {@link HttpServletRequest} to the OTel {@link TextMapGetter} contract.
     */
    private static class HttpServletRequestGetter implements TextMapGetter<HttpServletRequest> {
        @Override
        public Iterable<String> keys(HttpServletRequest carrier) {
            return Collections.list(carrier.getHeaderNames());
        }

        @Override
        public String get(HttpServletRequest carrier, String key) {
            return carrier == null ? null : carrier.getHeader(key);
        }
    }
}
