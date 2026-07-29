package io.github.loadup.gateway.core.engine;

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
