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
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import java.util.List;

/**
 * Array-based filter chain with cursor — no allocation per hop.
 *
 * <p>Each call to {@link #filter(GatewayContext)} advances the cursor
 * and invokes the next filter. When the cursor reaches the end of the
 * list, the call is a no-op (chain exhausted).
 */
public class DefaultFilterChain implements FilterChain {

    private final List<GatewayFilter> filters;
    private int cursor;

    public DefaultFilterChain(List<GatewayFilter> filters) {
        this.filters = filters != null ? List.copyOf(filters) : List.of();
        this.cursor = 0;
    }

    @Override
    public void filter(GatewayContext context) {
        if (cursor >= filters.size()) {
            return;
        }
        GatewayFilter next = filters.get(cursor);
        cursor++;
        next.filter(context, this);
    }
}
