package io.github.loadup.gateway.webmvc.support;

import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.RouteConfig;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

/**
 * Adapts a {@link ServerRequest} into the facade {@link GatewayRequest} model.
 */
public final class GatewayRequestFactory {

    private GatewayRequestFactory() {}

    public static GatewayRequest from(ServerRequest request, RouteConfig route) {
        HttpServletRequest servletRequest = request.servletRequest();
        String requestId = firstHeader(request, "X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        Map<String, String> headers = new HashMap<>();
        request.headers().asHttpHeaders().forEach((k, values) -> {
            if (values != null && !values.isEmpty()) {
                headers.put(k, values.get(0));
            }
        });

        Map<String, List<String>> queryParameters = new HashMap<>();
        MultiValueMap<String, String> params = request.params();
        if (params != null) {
            params.forEach((k, values) -> queryParameters.put(k, List.copyOf(values)));
        }

        Map<String, String> pathParameters = new HashMap<>();
        Map<String, Object> uriTemplateVariables = MvcUtils.getUriTemplateVariables(request);
        if (uriTemplateVariables != null) {
            uriTemplateVariables.forEach((name, value) -> pathParameters.put(name, String.valueOf(value)));
        }

        String body = readBody(request);
        String contentType = request.headers().contentType() != null
                ? request.headers().contentType().toString()
                : null;
        String clientIp = resolveClientIp(request, servletRequest);
        String userAgent = request.headers().firstHeader("User-Agent");

        Map<String, Object> attributes = new HashMap<>();
        if (body != null && !body.isBlank() && body.trim().startsWith("{")) {
            try {
                attributes.put("parsedBody", JsonUtil.toMap(body));
            } catch (Exception e) {
                // keep raw body only
            }
        }

        return new GatewayRequest(
                requestId,
                request.path(),
                request.method().name(),
                headers,
                queryParameters,
                pathParameters,
                body,
                contentType,
                clientIp,
                userAgent,
                LocalDateTime.now(),
                attributes);
    }

    private static String readBody(ServerRequest request) {
        try {
            // Use the SCG MVC body cache so the body can be read multiple times
            // (e.g. by a security strategy and then by the proxy handler).
            ByteArrayInputStream cached = MvcUtils.getOrCacheBody(request);
            if (cached == null) {
                return "";
            }
            return new String(cached.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static String resolveClientIp(ServerRequest request, HttpServletRequest servletRequest) {
        String ip = firstHeader(request, "X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            return (index != -1 ? ip.substring(0, index) : ip).trim();
        }
        ip = firstHeader(request, "X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return servletRequest.getRemoteAddr();
    }

    private static String firstHeader(ServerRequest request, String name) {
        return request.headers().firstHeader(name);
    }
}
