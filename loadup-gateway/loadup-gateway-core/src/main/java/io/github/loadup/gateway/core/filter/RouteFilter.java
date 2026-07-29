package io.github.loadup.gateway.core.filter;

import io.github.loadup.gateway.core.engine.DefaultGatewayEngine;
import io.github.loadup.gateway.core.router.RouteResolver;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Route resolution filter — the pivot between the global pre-chain and
 * the per-route dynamic sub-chain.
 *
 * <p>Resolves the {@link RouteConfig} for the current request, stores it
 * in the context, then delegates to {@link DefaultGatewayEngine#executeRouteChain}
 * to build and run the route-specific filter pipeline.
 */
@Slf4j
public class RouteFilter implements GatewayFilter {

    private final RouteResolver routeResolver;
    private final DefaultGatewayEngine engine;

    public RouteFilter(RouteResolver routeResolver, DefaultGatewayEngine engine) {
        this.routeResolver = routeResolver;
        this.engine = engine;
    }

    @Override
    public String name() {
        return "route";
    }

    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        Optional<RouteConfig> resolved = routeResolver.resolve(context.getRequest());
        if (resolved.isEmpty()) {
            throw GatewayExceptionFactory.routeNotFound(
                    context.getRequest().getMethod() + " " + context.getRequest().getPath());
        }
        RouteConfig route = resolved.get();
        context.setRoute(route);
        log.debug("Route resolved: {} → {}", route.getRouteId(), route.getTarget());

        // Delegate to engine for the dynamic per-route sub-chain
        engine.executeRouteChain(context, route);
    }
}
