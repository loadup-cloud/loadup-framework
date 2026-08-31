package io.github.loadup.gateway.webmvc.filter;

import io.github.loadup.commons.result.Result;
import io.github.loadup.commons.result.ResultMeta;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Wraps a successful backend response in the unified {@code {result, data, meta}} envelope.
 *
 * <p>Post-processing filter: it delegates to the next handler first and then rewrites the
 * response body. Wrapping is controlled per-route via {@code wrapResponse} or globally via
 * {@code loadup.gateway.response.wrap}. Error responses (HTTP &gt;= 400) are left untouched
 * because they are already formatted by the exception handler.
 */
public class ResponseWrapperHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(ResponseWrapperHandlerFilterFunction.class);

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

    private final GatewayProperties properties;

    public ResponseWrapperHandlerFilterFunction(GatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        ServerResponse response = next.handle(request);

        RouteConfig route = MvcUtils.getAttribute(request, GatewayAttributes.ROUTE_CONFIG);
        if (route == null || !shouldWrap(route) || response.statusCode().value() >= 400) {
            return response;
        }

        GatewayResponse proxyResponse = MvcUtils.getAttribute(request, GatewayAttributes.PROXY_RESPONSE);
        if (proxyResponse == null
                || proxyResponse.getBody() == null
                || proxyResponse.getBody().isBlank()) {
            return response;
        }

        try {
            Object data;
            try {
                data = JsonUtil.fromJson(proxyResponse.getBody(), Object.class);
            } catch (Exception e) {
                data = proxyResponse.getBody();
            }

            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("result", Result.buildSuccess());
            wrapper.put("data", data);
            wrapper.put("meta", ResultMeta.of(proxyResponse.getRequestId()));
            String wrappedBody = JsonUtil.toJson(wrapper);

            ServerResponse.BodyBuilder builder =
                    ServerResponse.status(response.statusCode()).contentType(MediaType.APPLICATION_JSON);
            response.headers().forEach((name, values) -> {
                if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase(Locale.ROOT))
                        && !HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)
                        && !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                    builder.header(name, values.toArray(new String[0]));
                }
            });
            return builder.body(wrappedBody);
        } catch (Exception e) {
            log.error("Failed to wrap response for route '{}'", route.getRouteId(), e);
            return response;
        }
    }

    private boolean shouldWrap(RouteConfig route) {
        if (route.getWrapResponse() != null) {
            return route.getWrapResponse();
        }
        if (properties != null && properties.getResponse() != null) {
            return properties.getResponse().isWrap();
        }
        return false;
    }
}
