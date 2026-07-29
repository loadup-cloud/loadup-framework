package io.github.loadup.gateway.core.filter;

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
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Distributed tracing filter — creates an OpenTelemetry span for each request.
 * Optional: only registered when a {@link Tracer} bean is present.
 */
public class TracingFilter implements GatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(TracingFilter.class);

    private final Tracer tracer;
    private final TextMapPropagator propagator;

    public TracingFilter(Tracer tracer, TextMapPropagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    @Override
    public String name() {
        return "tracing";
    }

    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        String routeId = context.getRoute() != null ? context.getRoute().getRouteId() : "unknown";
        Span span = tracer.spanBuilder("gateway:" + routeId).startSpan();
        try (Scope ignored = span.makeCurrent()) {
            chain.filter(context);
        } finally {
            span.end();
        }
    }
}
