package io.github.loadup.gateway.core.handler;

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

import io.github.loadup.gateway.core.router.RouteResolver;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.RouteConfig;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

/**
 * Spring MVC HandlerMapping that intercepts gateway requests.
 *
 * <p>Uses the RouteResolver (which supports both exact and pattern matching)
 * to determine if a request should be handled by the gateway. If a route
 * matches, returns a GatewayHandler with the pre-resolved RouteConfig
 * to avoid a redundant lookup in RouteAction.
 */
public class GatewayHandlerMapping extends AbstractHandlerMapping {

    private final RouteResolver routeResolver;

    public GatewayHandlerMapping(RouteResolver routeResolver) {
        this.routeResolver = routeResolver;
        setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Build a minimal GatewayRequest for route resolution
        // (only path and method are needed for matching)
        GatewayRequest lookupRequest =
                GatewayRequest.builder().path(path).method(method).build();

        // Use RouteResolver which handles both exact and pattern matching
        Optional<RouteConfig> route = routeResolver.resolve(lookupRequest);

        // Return GatewayHandler with resolved RouteConfig to avoid double-lookup
        return route.map(routeConfig -> new GatewayHandler(routeConfig.getRouteId(), routeConfig))
                .orElse(null);
    }
}
