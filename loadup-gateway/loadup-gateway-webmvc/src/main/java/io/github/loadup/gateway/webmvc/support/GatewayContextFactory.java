package io.github.loadup.gateway.webmvc.support;

import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.servlet.function.ServerRequest;

/**
 * Builds a {@link GatewayContext} for the facade security SPI from a {@link ServerRequest}.
 */
public final class GatewayContextFactory {

    private GatewayContextFactory() {}

    public static GatewayContext from(ServerRequest request, RouteConfig route) {
        GatewayRequest gatewayRequest = GatewayRequestFactory.from(request, route);
        Map<String, Object> attributes = new ConcurrentHashMap<>(
                gatewayRequest.getAttributes() == null ? Map.of() : gatewayRequest.getAttributes());
        return new GatewayContext(gatewayRequest, request.servletRequest(), null, null, route, attributes, null);
    }
}
