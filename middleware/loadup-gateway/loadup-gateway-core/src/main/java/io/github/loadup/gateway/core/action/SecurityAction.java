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

import io.github.loadup.gateway.core.security.SecurityStrategyManager;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Action to execute security checks based on RouteConfig.securityCode.
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityAction implements GatewayAction {

    private final SecurityStrategyManager strategyManager;

    @Override
    public void execute(GatewayContext context, GatewayActionChain chain) {
        RouteConfig route = context.getRoute();
        if (route == null) {
            // Should not happen if RouteAction is executed first
            chain.proceed(context);
            return;
        }

        String code = route.getSecurityCode();
        if (StringUtils.isBlank(code) || "OFF".equalsIgnoreCase(code)) {
            // Skip security
            chain.proceed(context);
            return;
        }

        SecurityStrategy strategy = strategyManager.getStrategy(code);
        if (strategy == null) {
            log.error("Security strategy not found for code: {}", code);
            // Fail safe: deny access if security is expected but missing
            throw GatewayExceptionFactory.systemError("Security strategy configured but missing: " + code);
        }

        try {
            log.debug("Executing security strategy: {} for route: {}", code, route.getRouteId());
            strategy.process(context);
        } catch (Exception e) {
            log.warn(
                    "Security check failed for request: {}",
                    context.getRequest().getRequestId(),
                    e);
            throw e; // Let exception handler handle it (e.g. 401/403)
        }

        chain.proceed(context);
    }
}
