package io.github.loadup.gateway.core.engine;

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
