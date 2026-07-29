package io.github.loadup.gateway.core.filter;

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
