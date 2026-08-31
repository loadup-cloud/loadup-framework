package io.github.loadup.gateway.webmvc.proxy;

import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import io.github.loadup.gateway.webmvc.support.GatewayRequestFactory;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Terminal handler that dispatches the request to the matching {@link ProxyProcessor}
 * (HTTP / BEAN / RPC) and converts the resulting {@link GatewayResponse} back into a
 * {@link ServerResponse}.
 */
public class ProxyHandlerFunction implements HandlerFunction<ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(ProxyHandlerFunction.class);

    /** Headers that must not be forwarded from the backend response to the client. */
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "content-length");

    private final ProxyProcessorRegistry processorRegistry;

    public ProxyHandlerFunction(ProxyProcessorRegistry processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    @Override
    public ServerResponse handle(ServerRequest request) throws Exception {
        RouteConfig route = MvcUtils.getAttribute(request, GatewayAttributes.ROUTE_CONFIG);
        if (route == null) {
            throw GatewayExceptionFactory.routeNotFound(request.method() + " " + request.path());
        }

        ProxyProcessor processor = processorRegistry.get(route.getProtocol());
        if (processor == null) {
            throw GatewayExceptionFactory.configurationError(
                    "No proxy processor registered for protocol: " + route.getProtocol());
        }

        GatewayRequest gatewayRequest = GatewayRequestFactory.from(request, route);
        long start = System.currentTimeMillis();
        try {
            GatewayResponse gatewayResponse = processor.proxy(gatewayRequest, route);
            gatewayResponse.setProcessingTime(System.currentTimeMillis() - start);
            MvcUtils.putAttribute(request, GatewayAttributes.PROXY_RESPONSE, gatewayResponse);
            return toServerResponse(gatewayResponse);
        } catch (Exception e) {
            log.error("Proxy dispatch failed for route '{}' protocol '{}'", route.getRouteId(), route.getProtocol(), e);
            throw GatewayExceptionFactory.wrap(e, route.getProtocol().toLowerCase(Locale.ROOT));
        }
    }

    private ServerResponse toServerResponse(GatewayResponse response) {
        HttpStatusCode status = HttpStatusCode.valueOf(response.getStatusCode());
        ServerResponse.BodyBuilder builder = ServerResponse.status(status);

        Map<String, String> headers = response.getHeaders();
        if (headers != null) {
            headers.forEach((name, value) -> {
                if (value != null && !HOP_BY_HOP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                    builder.header(name, value);
                }
            });
        }

        String contentType = response.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            builder.contentType(MediaType.parseMediaType(contentType));
        }

        String body = response.getBody();
        if (body == null || body.isBlank()) {
            return builder.build();
        }
        return builder.body(body);
    }
}
