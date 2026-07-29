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

import io.github.loadup.gateway.core.plugin.PluginManager;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backend proxy filter — delegates to the appropriate {@link io.github.loadup.gateway.facade.spi.ProxyProcessor}
 * based on the route protocol (http / bean / rpc).
 *
 * <p>This is the terminal filter in the request-phase chain; all filters
 * after it in the pipeline are response-phase (post-processing).</p>
 */
public class ProxyFilter implements GatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(ProxyFilter.class);

    private final PluginManager pluginManager;

    public ProxyFilter(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public String name() {
        return "proxy";
    }

    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        try {
            GatewayResponse response = pluginManager.executeProxy(context.getRequest(), context.getRoute());
            context.setResponse(response);
        } catch (Exception e) {
            log.error("Proxy execution failed for route: {}", context.getRoute().getRouteId(), e);
            throw io.github.loadup.gateway.facade.exception.GatewayExceptionFactory.systemError(
                    "Backend proxy failed: " + e.getMessage());
        }
        chain.filter(context); // Continue to response-phase filters
    }
}
