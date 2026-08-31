package io.github.loadup.gateway.webmvc.support;

/**
 * Request attribute keys used by the gateway engine.
 */
public final class GatewayAttributes {

    /** Compiled {@link io.github.loadup.gateway.facade.model.RouteConfig} for the matched route. */
    public static final String ROUTE_CONFIG = "loadup.gateway.routeConfig";

    /** Backend {@link io.github.loadup.gateway.facade.model.GatewayResponse} produced by the proxy handler. */
    public static final String PROXY_RESPONSE = "loadup.gateway.proxyResponse";

    private GatewayAttributes() {}
}
