package io.github.loadup.gateway.core.router;

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
        RouteConfig cached = exactRouteCache.get(routeKey);
        if (cached != null && cached.isEnabled()) {
            return Optional.of(cached);
        }
        Optional<RouteConfig> pm = patternRegistry.resolve(request.getMethod(), request.getPath());
        if (pm.isPresent()) {
            populatePathParams(request, pm.get());
        }
        return pm;
    }

    public void refreshRoutes() {
        try {
            List<RouteConfig> all = routeStore.loadAll().stream()
                    .filter(RouteDefinition::isEnabled)
                    .map(this::toRouteConfig)
                    .toList();
            ConcurrentHashMap<String, RouteConfig> next = new ConcurrentHashMap<>();
            for (RouteConfig r : all) {
                if (r.isEnabled()) next.put(buildRouteKey(r.getMethod(), r.getPath()), r);
            }
            patternRegistry.loadRoutes(all);
            this.exactRouteCache = next;
            log.info("Route cache refreshed: {} exact, {} total", next.size(), patternRegistry.size());
        } catch (Exception e) {
            log.error("Failed to refresh route cache", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void populatePathParams(GatewayRequest request, RouteConfig route) {
        Object pp = route.getProperties().get("_pathParams");
        if (pp instanceof Map) request.setPathParameters((Map<String, String>) pp);
        Object mp = route.getProperties().get("_matchedPattern");
        if (mp != null && request.getAttributes() != null)
            request.getAttributes().put("_matchedPattern", mp);
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

    private RouteConfig toRouteConfig(RouteDefinition def) {
        BackendDefinition backend = def.getBackend();
        RouteConfig rc = new RouteConfig();
        rc.setRouteId(
                def.getId() != null && !"auto".equals(def.getId())
                        ? def.getId()
                        : "route-" + Integer.toUnsignedString((def.getPath() + ":" + def.getMethod()).hashCode()));
        rc.setPath(def.getPath());
        rc.setMethod(def.getMethod() != null ? def.getMethod() : "POST");
        rc.setSecurityCode(def.getSecurityCode());
        rc.setEnabled(def.isEnabled());

        Map<String, Object> props = new HashMap<>();
        if (def.getTimeout() != null) props.put("timeout", def.getTimeout());
        if (def.getWrapResponse() != null) props.put("wrapResponse", def.getWrapResponse());
        rc.setProperties(props);

        if (backend != null && backend.getProtocol() != null) {
            String protocol = backend.getProtocol().toUpperCase();
            rc.setProtocol(protocol);
            switch (protocol) {
                case "HTTP" -> {
                    String url = backend.getUrl();
                    rc.setTarget(url != null ? url : "");
                    rc.setTargetUrl(url);
                }
                case "BEAN" -> {
                    String bn = backend.getBeanName() != null ? backend.getBeanName() : "";
                    String mn = backend.getMethodName() != null ? backend.getMethodName() : "";
                    rc.setTarget("bean://" + bn + ":" + mn);
                    rc.setTargetBean(bn);
                    rc.setTargetMethod(mn);
                }
                case "RPC" -> {
                    String rpcUrl = backend.getUrl();
                    rc.setTarget(rpcUrl != null ? "rpc://" + rpcUrl : "");
                    rc.setTargetUrl(rpcUrl);
                }
                default -> rc.setTarget("");
            }
        } else {
            rc.setProtocol("");
            rc.setTarget("");
        }
        return rc;
    }
}
