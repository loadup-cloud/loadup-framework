package com.github.loadup.components.gateway.core.controller;

import com.github.loadup.components.gateway.core.config.GatewayProperties;
import com.github.loadup.components.gateway.core.filter.GatewayFilter;
import com.github.loadup.components.gateway.core.route.RouteHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Gateway Controller
 * 网关主控制器，处理所有进入网关的请求
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class GatewayController {

    private final RouteHandler        routeHandler;
    private final List<GatewayFilter> filters;
    private final GatewayProperties   gatewayProperties;

    @RequestMapping("/**")
    public ResponseEntity<Object> handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getRequestURI();

        // 1. 匹配路由
        Optional<GatewayProperties.RouteDefinition> routeOpt = routeHandler.matchRoute(path);
        if (routeOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return ResponseEntity.notFound().build();
        }

        GatewayProperties.RouteDefinition route = routeOpt.get();
        request.setAttribute("route_id", route.getId());

        // 2. 执行过滤器链
        try {
            // 按order排序执行过滤器
            for (GatewayFilter filter : filters) {
                if (filter.isEnabled() && !filter.preHandle(request, response)) {
                    return ResponseEntity.status(response.getStatus()).build();
                }
            }

            // 3. 转发请求
            HttpHeaders headers = new HttpHeaders();
            Collections.list(request.getHeaderNames())
                .forEach(headerName -> headers.add(headerName, request.getHeader(headerName)));

            byte[] body = StreamUtils.copyToByteArray(request.getInputStream());

            Object result = routeHandler.forward(
                route.getId(),
                path,
                HttpMethod.valueOf(request.getMethod()),
                headers,
                body
            );

            // 4. 执行后置处理
            for (GatewayFilter filter : filters) {
                if (filter.isEnabled()) {
                    filter.postHandle(request, response);
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Request processing failed", e);
            // 5. 执行异常处理
            for (GatewayFilter filter : filters) {
                if (filter.isEnabled()) {
                    filter.afterCompletion(request, response, e);
                }
            }
            throw e;
        }
    }
}
