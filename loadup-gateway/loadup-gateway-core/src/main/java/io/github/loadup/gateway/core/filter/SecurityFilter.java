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

import io.github.loadup.gateway.core.security.SecurityStrategyManager;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security filter — delegates to a named {@link SecurityStrategy} based on
 * the route's {@code securityCode}.
 *
 * <p>The filter is always registered; it reads the security code from the
 * resolved route at runtime. If the code is {@code OFF} or blank, it skips.
 */
public class SecurityFilter implements GatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    private final SecurityStrategyManager strategyManager;

    @Override
    public String name() {
        return "security";
    }

    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        RouteConfig route = context.getRoute();
        if (route == null) {
            chain.filter(context);
            return;
        }

        String code = route.getSecurityCode();
        if (StringUtils.isBlank(code) || "OFF".equalsIgnoreCase(code)) {
            chain.filter(context);
            return;
        }

        SecurityStrategy strategy = strategyManager.getStrategy(code);
        if (strategy == null) {
            log.error("Unknown security code '{}' for route '{}'", code, route.getRouteId());
            throw GatewayExceptionFactory.systemError("Unknown security strategy: " + code);
        }

        try {
            strategy.process(context);
        } catch (Exception e) {
            log.warn("Security check failed: requestId={}", context.getRequest().getRequestId(), e);
            throw e;
        }

        chain.filter(context);
    }

    public SecurityFilter(SecurityStrategyManager strategyManager) {
        this.strategyManager = strategyManager;
    }
}
