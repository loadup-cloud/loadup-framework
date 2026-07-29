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
import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition.BackendDefinition;
import io.github.loadup.gateway.facade.spi.RouteStore;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteResolver {
    private static final Logger log = LoggerFactory.getLogger(RouteResolver.class);

    private final RouteStore routeStore;
    private final GatewayProperties gatewayProperties;
    private final PatternRouteRegistry patternRegistry;

    private volatile ConcurrentHashMap<String, RouteConfig> exactRouteCache = new ConcurrentHashMap<>();

    public RouteResolver(RouteStore routeStore, GatewayProperties gatewayProperties) {
        this.routeStore = routeStore;
        this.gatewayProperties = gatewayProperties;
        this.patternRegistry = new PatternRouteRegistry();
    }

    @PostConstruct
    public void refresh() {
        refreshRoutes();
    }

    public Optional<RouteConfig> resolve(GatewayRequest request) {
        String routeKey = buildRouteKey(request.getMethod(), request.getPath());

        ConcurrentHashMap<String, RouteConfig> currentCache = exactRouteCache;
        RouteConfig cachedRoute = currentCache.get(routeKey);
        if (cachedRoute != null && cachedRoute.isEnabled()) {
            return Optional.of(cachedRoute);
        }

        Optional<RouteConfig> patternMatch = patternRegistry.resolve(request.getMethod(), request.getPath());
        if (patternMatch.isPresent()) {
            RouteConfig matched = patternMatch.get();
            populatePathParams(request, matched);
            return Optional.of(matched);
        }

        return Optional.empty();
    }

    public void refreshRoutes() {
        try {
            List<RouteConfig> allRoutes = routeStore.loadAll().stream()
                    .filter(RouteDefinition::isEnabled)
                    .map(this::toRouteConfig)
                    .toList();

            ConcurrentHashMap<String, RouteConfig> newExactCache = new ConcurrentHashMap<>();
            for (RouteConfig route : allRoutes) {
                if (route.isEnabled()) {
                    String routeKey = buildRouteKey(route.getMethod(), route.getPath());
                    newExactCache.put(routeKey, route);
                }
            }

            patternRegistry.loadRoutes(allRoutes);
            this.exactRouteCache = newExactCache;

            log.info(
                    "Route cache refreshed: {} exact routes, {} total in pattern registry",
                    newExactCache.size(),
                    patternRegistry.size());
        } catch (Exception e) {
            log.error("Failed to refresh route cache", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void populatePathParams(GatewayRequest request, RouteConfig route) {
        Object pathParamsObj = route.getProperties().get("_pathParams");
        if (pathParamsObj instanceof Map) {
            request.setPathParameters((Map<String, String>) pathParamsObj);
        }
        Object matchedPattern = route.getProperties().get("_matchedPattern");
        if (matchedPattern != null && request.getAttributes() != null) {
            request.getAttributes().put("_matchedPattern", matchedPattern);
        }
    }

    private String buildRouteKey(String method, String path) {
        return method.toUpperCase() + ":" + path;
    }

    public int getCachedRouteCount() {
        return exactRouteCache.size();
    }

    public int getTotalRouteCount() {
        return patternRegistry.size();
    }

    /**
     * Convert RouteDefinition → RouteConfig for runtime.
     */
    private RouteConfig toRouteConfig(RouteDefinition def) {
        BackendDefinition backend = def.getBackend();
        String target = "";
        if (backend != null && backend.getProtocol() != null) {
            target = switch (backend.getProtocol().toLowerCase()) {
                case "http" -> backend.getUrl() != null ? backend.getUrl() : "";
                case "bean" ->
                    "bean://" + (backend.getBeanName() != null ? backend.getBeanName() : "") + ":"
                            + (backend.getMethodName() != null ? backend.getMethodName() : "");
                case "rpc" -> "rpc://" + (backend.getUrl() != null ? backend.getUrl() : "");
                default -> "";
            };
        }

        Map<String, Object> props = new HashMap<>();
        if (def.getTimeout() != null) props.put("timeout", def.getTimeout());
        if (def.getWrapResponse() != null) props.put("wrapResponse", def.getWrapResponse());

        RouteConfig routeConfig = new RouteConfig();
        routeConfig.setPath(def.getPath());
        routeConfig.setMethod(def.getMethod());
        routeConfig.setTarget(target);
        routeConfig.setSecurityCode(def.getSecurityCode());
        routeConfig.setEnabled(def.isEnabled());
        routeConfig.setProperties(props);
        return routeConfig;
    }
}
