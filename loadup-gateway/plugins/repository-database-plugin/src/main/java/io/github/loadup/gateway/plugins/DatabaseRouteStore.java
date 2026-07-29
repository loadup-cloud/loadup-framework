package io.github.loadup.gateway.plugins;

import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.model.FilterDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition.BackendDefinition;
import io.github.loadup.gateway.facade.spi.RouteStore;
import io.github.loadup.gateway.plugins.entity.RouteEntity;
import io.github.loadup.gateway.plugins.manager.RouteManager;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Database-backed RouteStore using Spring Data JDBC.
 *
 * <p>Stores route definitions in a relational database.
 * Supports full CRUD for the admin API.
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseRouteStore implements RouteStore {

    private final RouteManager routeManager;

    @PostConstruct
    public void init() {
        log.info("DatabaseRouteStore initialized ({} routes)", routeManager.count());
    }

    @Override
    public List<RouteDefinition> loadAll() {
        return StreamSupport.stream(routeManager.findAll().spliterator(), false)
                .filter(e -> Boolean.TRUE.equals(e.getEnabled()))
                .map(this::toRouteDefinition)
                .toList();
    }

    @Override
    public Optional<RouteDefinition> load(String routeId) {
        return routeManager.findById(routeId).map(this::toRouteDefinition);
    }

    @Override
    public RouteDefinition save(RouteDefinition def) {
        RouteEntity entity = toEntity(def);
        routeManager.save(entity);
        log.info("Route saved: id={}, path={} {}", entity.getId(), entity.getMethod(), entity.getPath());
        return def;
    }

    @Override
    public void delete(String routeId) {
        routeManager.deleteById(routeId);
        log.info("Route deleted: id={}", routeId);
    }

    // --- Conversion ---

    private RouteDefinition toRouteDefinition(RouteEntity e) {
        BackendDefinition backend = parseBackend(e.getTarget());

        return RouteDefinition.builder()
                .id(e.getId())
                .path(e.getPath())
                .method(e.getMethod() != null ? e.getMethod() : "POST")
                .enabled(Boolean.TRUE.equals(e.getEnabled()))
                .securityCode(e.getSecurityCode())
                .backend(backend)
                .filters(parseFilters(e.getRequestFilters(), e.getFilterProps()))
                .responseFilters(parseFilters(e.getResponseFilters(), e.getFilterProps()))
                .timeout(e.getTimeout())
                .wrapResponse(e.getWrapResponse())
                .build();
    }

    private RouteEntity toEntity(RouteDefinition def) {
        RouteEntity e = new RouteEntity();
        e.setId(def.getId());
        e.setPath(def.getPath());
        e.setMethod(def.getMethod());
        e.setTarget(toTargetString(def.getBackend()));
        e.setSecurityCode(def.getSecurityCode());
        e.setEnabled(def.isEnabled());
        e.setTimeout(def.getTimeout());
        e.setWrapResponse(def.getWrapResponse());
        e.setRequestFilters(toFilterNames(def.getFilters()));
        e.setResponseFilters(toFilterNames(def.getResponseFilters()));
        e.setFilterProps(toFilterProps(def));
        return e;
    }

    private BackendDefinition parseBackend(String target) {
        if (target == null || target.isBlank()) return BackendDefinition.builder().build();
        String t = target.trim();
        if (t.startsWith("http://") || t.startsWith("https://")) {
            return BackendDefinition.builder().protocol("http").url(t).build();
        }
        if (t.startsWith("bean://")) {
            String inner = t.substring(7);
            String[] parts = inner.split(":");
            return BackendDefinition.builder()
                    .protocol("bean")
                    .beanName(parts.length > 0 ? parts[0] : "")
                    .methodName(parts.length > 1 ? parts[1] : "")
                    .build();
        }
        if (t.startsWith("rpc://")) {
            return BackendDefinition.builder().protocol("rpc").url(t.substring(6)).build();
        }
        return BackendDefinition.builder().protocol("http").url(t).build();
    }

    private String toTargetString(BackendDefinition b) {
        if (b == null || b.getProtocol() == null) return "";
        return switch (b.getProtocol().toLowerCase()) {
            case "http" -> b.getUrl() != null ? b.getUrl() : "";
            case "bean" -> "bean://" + (b.getBeanName() != null ? b.getBeanName() : "")
                    + ":" + (b.getMethodName() != null ? b.getMethodName() : "");
            case "rpc" -> "rpc://" + (b.getUrl() != null ? b.getUrl() : "");
            default -> "";
        };
    }

    @SuppressWarnings("unchecked")
    private List<FilterDefinition> parseFilters(String names, String propsJson) {
        if (names == null || names.isBlank()) return Collections.emptyList();
        Map<String, Map<String, Object>> propsMap = parsePropsJson(propsJson);
        List<FilterDefinition> result = new ArrayList<>();
        for (String name : names.split(",")) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                Map<String, Object> filterProps = propsMap.getOrDefault(trimmed, Collections.emptyMap());
                result.add(FilterDefinition.builder().name(trimmed).props(new HashMap<>(filterProps)).build());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> parsePropsJson(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            Map<String, Object> raw = JsonUtil.toMap(json);
            Map<String, Map<String, Object>> result = new HashMap<>();
            raw.forEach((k, v) -> {
                if (v instanceof Map) result.put(k, (Map<String, Object>) v);
            });
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse filter props JSON: {}", json, e);
            return Collections.emptyMap();
        }
    }

    private String toFilterNames(List<FilterDefinition> filters) {
        return filters.stream().map(FilterDefinition::getName).reduce((a, b) -> a + "," + b).orElse("");
    }

    @SuppressWarnings("unchecked")
    private String toFilterProps(RouteDefinition def) {
        Map<String, Object> all = new HashMap<>();
        for (FilterDefinition fd : def.getFilters()) {
            if (fd.getProps() != null && !fd.getProps().isEmpty()) {
                all.put(fd.getName(), fd.getProps());
            }
        }
        for (FilterDefinition fd : def.getResponseFilters()) {
            if (fd.getProps() != null && !fd.getProps().isEmpty()) {
                all.put(fd.getName(), fd.getProps());
            }
        }
        return all.isEmpty() ? null : JsonUtil.toJson(all);
    }
}
