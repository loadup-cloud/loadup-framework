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

import io.github.loadup.gateway.core.router.RouteResolver;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

/** Action to resolve the route for the request. */
@Slf4j
public class RouteAction implements GatewayAction {

  private final RouteResolver routeResolver;

  public RouteAction(RouteResolver routeResolver) {
    this.routeResolver = routeResolver;
  }

  @Override
  public void execute(GatewayContext context, GatewayActionChain chain) {
    // Resolve route
    Optional<RouteConfig> routeOpt = routeResolver.resolve(context.getRequest());

    if (!routeOpt.isPresent()) {
      // Throw exception to be handled by the adapter
      throw GatewayExceptionFactory.routeNotFound(context.getRequest().getPath());
    }

    RouteConfig route = routeOpt.get();
    log.debug("Route resolved: {} -> {}", context.getRequest().getPath(), route.getRouteId());

    // Store route in context
    context.setRoute(route);

    // Proceed
    chain.proceed(context);
  }

  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1000;
  }
}
