package io.github.loadup.gateway.core.engine;

import io.github.loadup.gateway.facade.context.GatewayContext;

/**
 * Central gateway engine — resolves routes, builds per-route filter chains, and executes them.
 *
 * <p>This replaces the hardcoded ActionDispatcher with a dynamic model where
 * each route declares its own filter pipeline in YAML configuration.
 *
 * <p>Execution flow:
 * <ol>
 *   <li>Resolve route via {@link io.github.loadup.gateway.core.router.RouteResolver}</li>
 *   <li>Build filter chain: [defaultFilters] + route.filters + [proxy] + route.responseFilters</li>
 *   <li>Execute the chain against the context</li>
 * </ol>
 */
public interface GatewayEngine {

    /**
     * Process an incoming request through the gateway pipeline.
     *
     * @param context the gateway context with the incoming request
     */
    void execute(GatewayContext context);
}
