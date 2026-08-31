/*-
 * #%L
 * Loadup Gateway WebMVC Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.gateway.webmvc.filter;

import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Optional OpenTelemetry tracing for gateway routes.
 */
public class TracingHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(TracingHandlerFilterFunction.class);

    private final Tracer tracer;

    public TracingHandlerFilterFunction(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        RouteConfig route = MvcUtils.getAttribute(request, GatewayAttributes.ROUTE_CONFIG);
        String spanName = route != null ? "gateway.route." + route.getRouteId() : "gateway.route";
        Span span = tracer.spanBuilder(spanName).setSpanKind(SpanKind.SERVER).startSpan();
        if (route != null) {
            span.setAttribute("gateway.route.id", route.getRouteId());
        }
        span.setAttribute("http.request.method", request.method().name());
        span.setAttribute("url.path", request.path());
        try (Scope ignored = span.makeCurrent()) {
            ServerResponse response = next.handle(request);
            span.setAttribute("http.response.status_code", response.statusCode().value());
            return response;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }
}
