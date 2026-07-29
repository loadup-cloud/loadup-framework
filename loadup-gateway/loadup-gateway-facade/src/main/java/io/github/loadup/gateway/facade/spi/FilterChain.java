package io.github.loadup.gateway.facade.spi;

import io.github.loadup.gateway.facade.context.GatewayContext;

/**
 * Filter chain — invokes the next filter in sequence for the current route.
 *
 * <p>A filter calls {@code chain.filter(context)} to pass control downstream.
 * When the chain is exhausted, the backend proxy is invoked before unwinding.
 */
@FunctionalInterface
public interface FilterChain {

    /**
     * Invoke the next filter (or backend proxy) with the given context.
     *
     * @param context the gateway context
     */
    void filter(GatewayContext context);
}
