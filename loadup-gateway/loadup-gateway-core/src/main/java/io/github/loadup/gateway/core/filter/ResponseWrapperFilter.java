package io.github.loadup.gateway.core.filter;

import io.github.loadup.commons.result.Result;
import io.github.loadup.commons.result.ResultMeta;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Response wrapper filter — wraps backend responses in standard {@code {result, data, meta}} format.
 *
 * <p>Runs after the proxy has populated {@code context.getResponse()}.
 * Respects per-route {@code wrapResponse} and global {@code loadup.gateway.response.wrap} config.
 */
@Slf4j
public class ResponseWrapperFilter implements GatewayFilter {

    private final GatewayProperties gatewayProperties;

    public ResponseWrapperFilter(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public String name() {
        return "response-wrapper";
    }

    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        chain.filter(context);

        GatewayResponse response = context.getResponse();
        RouteConfig route = context.getRoute();

        if (response == null || route == null) return;

        boolean shouldWrap;
        if (route.getWrapResponse() != null) {
            shouldWrap = route.getWrapResponse();
        } else if (gatewayProperties.getResponse() != null) {
            shouldWrap = gatewayProperties.getResponse().isWrap();
        } else {
            shouldWrap = false;
        }

        // Skip wrapping for error responses (already wrapped by ExceptionFilter)
        if (response.getStatusCode() >= 400) return;

        if (shouldWrap) {
            try {
                Object data = response.getBody() != null
                        ? JsonUtil.fromJson(response.getBody(), Object.class)
                        : null;
                if (data == null) data = response.getBody();

                Map<String, Object> wrapper = new LinkedHashMap<>();
                wrapper.put("result", Result.buildSuccess());
                wrapper.put("data", data);
                wrapper.put("meta", ResultMeta.of(context.getRequest().getRequestId()));

                String newBody = JsonUtil.toJson(wrapper);
                response.setBody(newBody);
                if (response.getHeaders() == null) response.setHeaders(new HashMap<>());
                response.getHeaders().put("Content-Type", "application/json");
                response.getHeaders().put("Content-Length",
                        String.valueOf(newBody.getBytes(StandardCharsets.UTF_8).length));
            } catch (Exception e) {
                log.error("Failed to wrap response", e);
            }
        }
    }
}
