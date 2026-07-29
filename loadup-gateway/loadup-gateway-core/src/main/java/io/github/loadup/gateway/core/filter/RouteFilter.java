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

import io.github.loadup.gateway.core.engine.DefaultGatewayEngine;
import io.github.loadup.gateway.core.router.RouteResolver;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Route resolution filter — the pivot between the global pre-chain and
 * the per-route dynamic sub-chain.
 *
 * <p>Resolves the {@link RouteConfig} for the current request, stores it
 * in the context, then delegates to {@link DefaultGatewayEngine#executeRouteChain}
 * to build and run the route-specific filter pipeline.
 */
public class RouteFilter implements GatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(RouteFilter.class);

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
            throw GatewayExceptionFactory.routeNotFound(context.getRequest().getMethod() + " "
                    + context.getRequest().getPath());
        }
        RouteConfig route = resolved.get();
        context.setRoute(route);
        log.debug("Route resolved: {} → {}", route.getRouteId(), route.getTarget());

        // Delegate to engine for the dynamic per-route sub-chain
        engine.executeRouteChain(context, route);
    }
}
