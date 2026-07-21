package io.github.loadup.gateway.core.router;

/*-
 * #%L
 * LoadUp Gateway Core
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.RepositoryPlugin;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Route resolver with Ant-style path pattern support.
 *
 * <p>Maintains a two-tier cache:
 * <ul>
 *   <li>Exact-match cache (ConcurrentHashMap) — fast O(1) lookup for literal paths</li>
 *   <li>PatternRouteRegistry — ordered pattern matching for paths with variables like /api/user/{id}</li>
 * </ul>
 *
 * <p>Cache refresh uses double-buffering with atomic swap to avoid
 * the clear-to-populate window.
 */
@Slf4j
public class RouteResolver {

    private final RepositoryPlugin repositoryPlugin;
    private final GatewayProperties gatewayProperties;
    private final PatternRouteRegistry patternRegistry;

    public RouteResolver(RepositoryPlugin repositoryPlugin, GatewayProperties gatewayProperties) {
        this.repositoryPlugin = repositoryPlugin;
        this.gatewayProperties = gatewayProperties;
        this.patternRegistry = new PatternRouteRegistry();
    }

    private volatile ConcurrentHashMap<String, RouteConfig> exactRouteCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void refresh() {
        repositoryPlugin.initialize();
        refreshRoutes();
    }

    /**
     * Resolve the route configuration for the given request.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>Exact-match cache (method:path → RouteConfig)</li>
     *   <li>Pattern registry (e.g., /api/user/{id})</li>
     *   <li>Storage fallback (repository plugin)</li>
     * </ol>
     *
     * <p>When a pattern route matches, its path variables are extracted
     * and stored in the request's pathParameters map and route properties.
     */
    public Optional<RouteConfig> resolve(GatewayRequest request) {
        String routeKey = buildRouteKey(request.getMethod(), request.getPath());

        // 1. Exact-match cache
        ConcurrentHashMap<String, RouteConfig> currentCache = exactRouteCache;
        RouteConfig cachedRoute = currentCache.get(routeKey);
        if (cachedRoute != null && cachedRoute.isEnabled()) {
            return Optional.of(cachedRoute);
        }

        // 2. Pattern matching
        Optional<RouteConfig> patternMatch = patternRegistry.resolve(request.getMethod(), request.getPath());
        if (patternMatch.isPresent()) {
            RouteConfig matched = patternMatch.get();
            // Populate path parameters into the request
            populatePathParams(request, matched);
            return Optional.of(matched);
        }

        // 3. Storage fallback
        try {
            Optional<RouteConfig> routeOpt =
                    repositoryPlugin.getRouteByPath(request.getPath(), request.getMethod());
            if (routeOpt.isPresent() && routeOpt.get().isEnabled()) {
                exactRouteCache.put(routeKey, routeOpt.get());
                return routeOpt;
            }
        } catch (Exception e) {
            log.error("Failed to resolve route from repository", e);
        }

        return Optional.empty();
    }

    /**
     * Refresh route cache using double-buffering to eliminate the clear-to-populate
     * window where concurrent requests would see an empty cache and cause a
     * database/disk thundering herd.
     */
    public void refreshRoutes() {
        try {
            List<RouteConfig> allRoutes = repositoryPlugin.getAllRoutes();

            // Build new exact-match cache
            ConcurrentHashMap<String, RouteConfig> newExactCache = new ConcurrentHashMap<>();
            for (RouteConfig route : allRoutes) {
                if (route.isEnabled()) {
                    String routeKey = buildRouteKey(route.getMethod(), route.getPath());
                    newExactCache.put(routeKey, route);
                }
            }

            // Load into pattern registry (handles its own atomic swap internally)
            patternRegistry.loadRoutes(allRoutes);

            // Atomic reference swap for exact-match cache
            this.exactRouteCache = newExactCache;

            log.info(
                    "Route cache refreshed: {} exact routes, {} total in pattern registry",
                    newExactCache.size(),
                    patternRegistry.size());

        } catch (Exception e) {
            log.error("Failed to refresh route cache", e);
        }
    }

    /**
     * Extract path variables from a pattern-matched route and populate
     * them into the request's pathParameters.
     */
    @SuppressWarnings("unchecked")
    private void populatePathParams(GatewayRequest request, RouteConfig route) {
        Object pathParamsObj = route.getProperties().get("_pathParams");
        if (pathParamsObj instanceof Map) {
            Map<String, String> pathParams = (Map<String, String>) pathParamsObj;
            request.setPathParameters(pathParams);
        }

        // Also store the matched pattern for debugging
        Object matchedPattern = route.getProperties().get("_matchedPattern");
        if (matchedPattern != null && request.getAttributes() != null) {
            request.getAttributes().put("_matchedPattern", matchedPattern);
        }
    }

    /**
     * Build route cache key.
     */
    private String buildRouteKey(String method, String path) {
        return method.toUpperCase() + ":" + path;
    }

    /**
     * Get the number of cached routes (exact-match only).
     */
    public int getCachedRouteCount() {
        return exactRouteCache.size();
    }

    /**
     * Get the total number of registered routes (exact + pattern).
     */
    public int getTotalRouteCount() {
        return patternRegistry.size();
    }
}
