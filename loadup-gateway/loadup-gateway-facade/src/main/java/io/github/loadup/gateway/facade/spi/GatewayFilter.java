package io.github.loadup.gateway.facade.spi;

/*-
 * #%L
 * LoadUp Gateway Facade
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
 * Named gateway filter — the fundamental building block of the gateway pipeline.
 *
 * <p>Each filter has a unique {@link #name()} used for route-level declaration
 * in YAML config. Filters are ordered per-route via the route definition's
 * {@code filters} list, not by a hardcoded global chain.
 *
 * <p>Pre-built filters (rateLimit, security, circuitBreaker, bodyParser, etc.)
 * are registered as Spring beans and discovered by name. Users can add custom
 * filters by implementing this interface and registering a bean.
 *
 * <p>A filter that processes the request before passing to the next filter
 * calls {@code chain.filter(context)} in the middle of its logic. A
 * post-processing filter (e.g. response wrapping) calls {@code chain.filter()}
 * first, then operates on {@code context.getResponse()}.
 */
public interface GatewayFilter {

    /**
     * Unique filter name used in route YAML declarations.
     * Must be stable, lowercase, and kebab-case by convention.
     * Examples: "rate-limit", "jwt-auth", "body-parser"
     */
    String name();

    /**
     * Execute this filter's logic on the given context, then (optionally)
     * pass to the next filter in the chain.
     *
     * @param context the gateway context for the current request
     * @param chain   the next filter to invoke
     */
    void filter(GatewayContext context, FilterChain chain);
}
