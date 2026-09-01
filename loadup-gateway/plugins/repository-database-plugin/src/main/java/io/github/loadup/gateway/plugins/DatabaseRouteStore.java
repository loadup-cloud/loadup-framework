package io.github.loadup.gateway.plugins;

/*-
 * #%L
 * Repository Database Plugin
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database-backed RouteStore using Spring Data JDBC.
 *
 * <p>Stores route definitions in a relational database.
 * Supports full CRUD for the admin API.
 */
public class DatabaseRouteStore implements RouteStore {
    private static final Logger log = LoggerFactory.getLogger(DatabaseRouteStore.class);

    private final RouteManager routeManager;

    @PostConstruct
    public void init() {
        log.info("DatabaseRouteStore initialized ({} routes)", routeManager.count());
    }

    @Override
    public List<RouteDefinition> loadAll() {
        return StreamSupport.stream(routeManager.findAll().spliterator(), false)
                .filter(e -> Boolean.TRUE.equals(e.isEnabled()))
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
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(e.getId());
        routeDefinition.setPath(e.getPath());
        routeDefinition.setMethod(e.getMethod() != null ? e.getMethod() : "POST");
        routeDefinition.setEnabled(Boolean.TRUE.equals(e.isEnabled()));
        routeDefinition.setSecurityCode(e.getSecurityCode());
        routeDefinition.setAuthorize(e.getAuthorize());
        routeDefinition.setBackend(backend);
        routeDefinition.setFilters(parseFilters(e.getRequestFilters(), e.getFilterProps()));
        routeDefinition.setResponseFilters(parseFilters(e.getResponseFilters(), e.getFilterProps()));
        routeDefinition.setTimeout(e.getTimeout());
        routeDefinition.setWrapResponse(e.isWrapResponse());
        return routeDefinition;
    }

    private RouteEntity toEntity(RouteDefinition def) {
        RouteEntity e = new RouteEntity();
        e.setId(def.getId());
        e.setPath(def.getPath());
        e.setMethod(def.getMethod());
        e.setTarget(toTargetString(def.getBackend()));
        e.setSecurityCode(def.getSecurityCode());
        e.setAuthorize(def.getAuthorize());
        e.setEnabled(def.isEnabled());
        e.setTimeout(def.getTimeout());
        e.setWrapResponse(def.getWrapResponse());
        e.setRequestFilters(toFilterNames(def.getFilters()));
        e.setResponseFilters(toFilterNames(def.getResponseFilters()));
        e.setFilterProps(toFilterProps(def));
        return e;
    }

    private BackendDefinition parseBackend(String target) {
        if (target == null || target.isBlank()) return new BackendDefinition();
        String t = target.trim();
        if (t.startsWith("http://") || t.startsWith("https://")) {
            BackendDefinition backendDefinition = new BackendDefinition();
            backendDefinition.setProtocol("http");
            backendDefinition.setUrl(t);
            return backendDefinition;
        }
        if (t.startsWith("bean://")) {
            String inner = t.substring(7);
            String[] parts = inner.split(":");
            BackendDefinition backendDefinition = new BackendDefinition();
            backendDefinition.setProtocol("bean");
            backendDefinition.setBeanName(parts.length > 0 ? parts[0] : "");
            backendDefinition.setMethodName(parts.length > 1 ? parts[1] : "");
            return backendDefinition;
        }
        if (t.startsWith("rpc://")) {

            BackendDefinition backendDefinition = new BackendDefinition();
            backendDefinition.setProtocol("rpc");
            backendDefinition.setUrl(t.substring(6));
            return backendDefinition;
        }
        BackendDefinition backendDefinition = new BackendDefinition();
        backendDefinition.setProtocol("http");
        backendDefinition.setUrl(t);
        return backendDefinition;
    }

    private String toTargetString(BackendDefinition b) {
        if (b == null || b.getProtocol() == null) return "";
        return switch (b.getProtocol().toLowerCase()) {
            case "http" -> b.getUrl() != null ? b.getUrl() : "";
            case "bean" ->
                "bean://" + (b.getBeanName() != null ? b.getBeanName() : "") + ":"
                        + (b.getMethodName() != null ? b.getMethodName() : "");
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
                result.add(FilterDefinition.builder()
                        .name(trimmed)
                        .props(new HashMap<>(filterProps))
                        .build());
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
        return filters.stream()
                .map(FilterDefinition::getName)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
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

    public DatabaseRouteStore(RouteManager routeManager) {
        this.routeManager = routeManager;
    }
}
