package io.github.loadup.gateway.core.filter;

import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import lombok.extern.slf4j.Slf4j;

/**
 * Distributed tracing filter — creates an OpenTelemetry span for each request.
 * Optional: only registered when a {@link Tracer} bean is present.
 */
@Slf4j
public class TracingFilter implements GatewayFilter {

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
