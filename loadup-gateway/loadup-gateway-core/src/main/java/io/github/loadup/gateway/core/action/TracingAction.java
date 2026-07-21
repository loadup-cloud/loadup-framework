package io.github.loadup.gateway.core.action;

/*-
 * #%L
 * LoadUp Gateway Core
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

import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tracing action for distributed tracing in LoadUp Gateway.
 *
 * <p>This action:
 * <ul>
 *   <li>Extracts tracing context from incoming requests</li>
 *   <li>Creates a span for the gateway request</li>
 *   <li>Propagates tracing context to downstream services</li>
 *   <li>Records request/response information</li>
 * </ul>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TracingAction implements GatewayAction {

    private final Tracer tracer;
    private final TextMapPropagator propagator;

    @Override
    public void execute(GatewayContext context, GatewayActionChain chain) {
        GatewayRequest request = context.getRequest();
        GatewayResponse response = context.getResponse();

        // Extract parent context from incoming request headers
        Context extractedContext = propagator.extract(Context.current(), request, GatewayRequestGetter.INSTANCE);

        // Create span for this gateway request
        String routeId = context.getRoute() != null ? context.getRoute().getRouteId() : "unknown";
        Span span = tracer.spanBuilder("gateway." + request.getMethod())
                .setParent(extractedContext)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("http.method", request.getMethod())
                .setAttribute("http.target", request.getPath())
                .setAttribute("gateway.route", routeId)
                .setAttribute("gateway.request_id", request.getRequestId())
                .startSpan();

        // Add client IP if available
        if (request.getClientIp() != null) {
            span.setAttribute("http.client_ip", request.getClientIp());
        }

        // Make span current and propagate to downstream
        try (Scope ignored = span.makeCurrent()) {
            // Inject tracing context into request headers (for downstream)
            propagator.inject(Context.current(), request, GatewayRequestSetter.INSTANCE);

            // Continue the chain
            chain.proceed(context);

            // Record response status
            int statusCode = response.getStatusCode();
            span.setAttribute("http.status_code", statusCode);

            if (statusCode >= 200 && statusCode < 300) {
                span.setStatus(StatusCode.OK);
            } else if (statusCode >= 400 && statusCode < 500) {
                span.setStatus(StatusCode.ERROR, "Client error: " + statusCode);
            } else if (statusCode >= 500) {
                span.setStatus(StatusCode.ERROR, "Server error: " + statusCode);
            }

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * TextMapGetter for extracting headers from GatewayRequest.
     */
    private static class GatewayRequestGetter implements TextMapGetter<GatewayRequest> {
        static final GatewayRequestGetter INSTANCE = new GatewayRequestGetter();

        @Override
        public Iterable<String> keys(GatewayRequest request) {
            Map<String, String> headers = request.getHeaders();
            return headers != null ? headers.keySet() : Collections.emptySet();
        }

        @Override
        public String get(GatewayRequest request, String key) {
            Map<String, String> headers = request.getHeaders();
            return headers != null ? headers.get(key) : null;
        }
    }

    /**
     * TextMapSetter for injecting headers into GatewayRequest.
     */
    private static class GatewayRequestSetter implements TextMapSetter<GatewayRequest> {
        static final GatewayRequestSetter INSTANCE = new GatewayRequestSetter();

        @Override
        public void set(GatewayRequest request, String key, String value) {
            if (request.getHeaders() != null) {
                request.getHeaders().put(key, value);
            }
        }
    }
}
