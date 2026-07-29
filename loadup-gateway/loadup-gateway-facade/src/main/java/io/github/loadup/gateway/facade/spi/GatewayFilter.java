package io.github.loadup.gateway.facade.spi;

import io.github.loadup.gateway.facade.context.GatewayContext;

/**
 * Named gateway filter — the fundamental building block of the gateway pipeline.
 *
 * <p>Each filter has a unique {@link #name()} used for route-level declaration
 * in YAML config. Filters are ordered per-route via the route definition's
 * {@code filters} list, not by a hardcoded global chain.
 *
 * <p>Pre-built filters (rateLimit, security, circuitBreaker, bodyParser, etc.)
 * are registered as Spring beans and discovered by name. Users can add custom
 * filters by implementing this interface and registering a bean.
 *
 * <p>A filter that processes the request before passing to the next filter
 * calls {@code chain.filter(context)} in the middle of its logic. A
 * post-processing filter (e.g. response wrapping) calls {@code chain.filter()}
 * first, then operates on {@code context.getResponse()}.
 */
public interface GatewayFilter {

    /**
     * Unique filter name used in route YAML declarations.
     * Must be stable, lowercase, and kebab-case by convention.
     * Examples: "rate-limit", "jwt-auth", "body-parser"
     */
    String name();

    /**
     * Execute this filter's logic on the given context, then (optionally)
     * pass to the next filter in the chain.
     *
     * @param context the gateway context for the current request
     * @param chain   the next filter to invoke
     */
    void filter(GatewayContext context, FilterChain chain);
}
