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

import io.github.loadup.gateway.core.plugin.PluginManager;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

/** Action to execute the proxy request. */
@Slf4j
public class ProxyAction implements GatewayAction {

    private final PluginManager pluginManager;

    public ProxyAction(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void execute(GatewayContext context, GatewayActionChain chain) {
        try {
            // Execute proxy through plugin manager
            GatewayResponse response = pluginManager.executeProxy(context.getRequest(), context.getRoute());

            // Set response in context
            context.setResponse(response);

            // Proceed (though typically this is the end)
            chain.proceed(context);
        } catch (Exception e) {
            // Re-throw to be handled by adapter (or wrap if needed, but adapter handles generic
            // Exception)
            throw new RuntimeException("Proxy execution failed", e);
        }
    }

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1000;
    }
}
