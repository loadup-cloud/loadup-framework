package io.github.loadup.gateway.core.handler;

import io.github.loadup.gateway.core.engine.GatewayEngine;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

public class GatewayHandlerAdapter implements HandlerAdapter, Ordered {

    private final GatewayEngine engine;

    public GatewayHandlerAdapter(GatewayEngine engine) {
        this.engine = engine;
    }

    @Override
    public boolean supports(Object handler) {
        return handler instanceof GatewayHandler;
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        GatewayContext context = buildGatewayContext(request, response);
        engine.execute(context);

        if (context.getResponse() != null) {
            writeResponse(response, context.getResponse());
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No response generated");
        }
        return null;
    }

    private GatewayContext buildGatewayContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        GatewayRequest gatewayRequest = buildGatewayRequest(request);
        return GatewayContext.builder()
                .request(gatewayRequest)
                .originalRequest(request)
                .originalResponse(response)
                .build();
    }

    private GatewayRequest buildGatewayRequest(HttpServletRequest request) throws IOException {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }

        Map<String, List<String>> queryParams = new HashMap<>();
        if (request.getQueryString() != null) {
            Arrays.stream(request.getQueryString().split("&")).forEach(param -> {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    queryParams.computeIfAbsent(kv[0], k -> new ArrayList<>()).add(kv[1]);
                }
            });
        }

        String body = request.getReader().lines().collect(Collectors.joining("\n"));

        return GatewayRequest.builder()
                .requestId(UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .path(request.getRequestURI())
                .method(request.getMethod())
                .headers(headers)
                .queryParameters(queryParams)
                .body(body)
                .contentType(request.getContentType())
                .clientIp(extractClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .requestTime(LocalDateTime.now())
                .attributes(new HashMap<>())
                .build();
    }

    /**
     * Extract the real client IP, respecting standard proxy headers.
     */
    private String extractClientIp(HttpServletRequest request) {
        for (String header : List.of("X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP")) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private void writeResponse(HttpServletResponse response, GatewayResponse gatewayResponse)
            throws IOException {
        response.setStatus(gatewayResponse.getStatusCode());

        if (gatewayResponse.getHeaders() != null) {
            gatewayResponse.getHeaders().forEach((k, v) -> {
                if (k == null || v == null) return;
                String lower = k.toLowerCase(Locale.ROOT);
                if ("content-length".equals(lower) || "transfer-encoding".equals(lower)) return;
                response.setHeader(k, v);
            });
        }

        if (gatewayResponse.getContentType() != null) {
            response.setContentType(gatewayResponse.getContentType());
        } else if (gatewayResponse.getBody() != null && gatewayResponse.getBody().trim().startsWith("{")) {
            response.setContentType("application/json;charset=UTF-8");
        }

        if (gatewayResponse.getBody() != null) {
            response.getWriter().write(gatewayResponse.getBody());
            response.getWriter().flush();
        } else {
            response.getWriter().flush();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
