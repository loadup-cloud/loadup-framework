package com.github.loadup.components.gateway.core.route;

import com.github.loadup.components.gateway.core.config.GatewayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Route Handler
 * 路由处理器，负责请求的实际转发
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteHandler {

    private final GatewayProperties                              gatewayProperties;
    private final RestTemplate                                   restTemplate;
    private final Map<String, GatewayProperties.RouteDefinition> routeCache = new ConcurrentHashMap<>();

    /**
     * 根据请求路径匹配路由
     */
    public Optional<GatewayProperties.RouteDefinition> matchRoute(String path) {
        // 先从缓存中查找
        return gatewayProperties.getRoutes().stream()
            .filter(route -> matchPredicate(route, path))
            .findFirst();
    }

    /**
     * 转发请求
     */
    public Object forward(String routeId, String path, HttpMethod method, HttpHeaders headers, byte[] body) {
        GatewayProperties.RouteDefinition route = routeCache.computeIfAbsent(routeId,
            id -> gatewayProperties.getRoutes().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Route not found: " + id)));

        // 构建目标URI
        String targetUrl = buildTargetUrl(route.getUri(), path);

        // 应用过滤器
        applyFilters(route, headers);

        // 执行请求转发
        return restTemplate.exchange(
            targetUrl,
            method,
            new org.springframework.http.HttpEntity<>(body, headers),
            Object.class
        ).getBody();
    }

    private boolean matchPredicate(GatewayProperties.RouteDefinition route, String path) {
        return route.getPredicates().stream()
            .anyMatch(predicate -> {
                switch (predicate.getName().toLowerCase()) {
                    case "path":
                        return matchPath(predicate.getArgs(), path);
                    case "method":
                        return matchMethod(predicate.getArgs(), path);
                    default:
                        return false;
                }
            });
    }

    private boolean matchPath(List<String> patterns, String path) {
        return patterns.stream()
            .anyMatch(pattern -> {
                pattern = pattern.replace("/**", ".*")
                    .replace("/*", "[^/]*");
                return path.matches(pattern);
            });
    }

    private boolean matchMethod(List<String> methods, String requestMethod) {
        return methods.isEmpty() || methods.contains(requestMethod.toUpperCase());
    }

    private String buildTargetUrl(String baseUrl, String path) {
        return URI.create(baseUrl).resolve(path).toString();
    }

    private void applyFilters(GatewayProperties.RouteDefinition route, HttpHeaders headers) {
        route.getFilters().forEach(filter -> {
            switch (filter.getName().toLowerCase()) {
                case "addheader":
                    if (filter.getArgs().size() >= 2) {
                        headers.add(filter.getArgs().get(0), filter.getArgs().get(1));
                    }
                    break;
                case "removeheader":
                    if (!filter.getArgs().isEmpty()) {
                        headers.remove(filter.getArgs().get(0));
                    }
                    break;
                case "prefix":
                    if (!filter.getArgs().isEmpty()) {
                        headers.add("X-Forwarded-Prefix", filter.getArgs().get(0));
                    }
                    break;
            }
        });
    }
}
