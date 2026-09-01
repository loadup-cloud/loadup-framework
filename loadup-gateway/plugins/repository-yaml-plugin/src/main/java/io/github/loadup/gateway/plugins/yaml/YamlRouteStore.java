/*-
 * #%L
 * Repository YAML Plugin
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
package io.github.loadup.gateway.plugins.yaml;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.event.RouteStoreRefreshedEvent;
import io.github.loadup.gateway.facade.model.FilterDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition.BackendDefinition;
import io.github.loadup.gateway.facade.spi.RouteStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

public class YamlRouteStore implements RouteStore {
    private static final Logger log = LoggerFactory.getLogger(YamlRouteStore.class);
    private static final String DEFAULT_CONFIG_FILE = "gateway-routes.yml";

    private final GatewayProperties gatewayProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final Yaml yaml = new Yaml(
            new SafeConstructor(new LoaderOptions()),
            new Representer(new DumperOptions()),
            new DumperOptions(),
            new LoaderOptions(),
            new StrictBooleanResolver());
    private final AtomicReference<List<RouteDefinition>> routes = new AtomicReference<>(List.of());

    private String classpathResource;
    private Path configPath;
    private WatchService watchService;
    private ScheduledExecutorService watchExecutor;
    private volatile boolean watching;

    public YamlRouteStore(GatewayProperties gatewayProperties, ApplicationEventPublisher eventPublisher) {
        this.gatewayProperties = gatewayProperties;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        resolveConfig();
        log.info("YamlRouteStore config: fsPath={}, classpath={}", configPath, classpathResource);
        reload();
        if (configPath != null) {
            startFileWatcher();
        }
    }

    @PreDestroy
    public void destroy() {
        watching = false;
        if (watchExecutor != null) {
            watchExecutor.shutdownNow();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public List<RouteDefinition> loadAll() {
        return Collections.unmodifiableList(routes.get());
    }

    @Override
    public Optional<RouteDefinition> load(String routeId) {
        return routes.get().stream().filter(r -> r.getId().equals(routeId)).findFirst();
    }

    private void resolveConfig() {
        String basePath = null;
        if (gatewayProperties.getStorage() != null
                && gatewayProperties.getStorage().getFile() != null) {
            basePath = gatewayProperties.getStorage().getFile().getBasePath();
        }

        if (basePath != null && !basePath.isBlank()) {
            if (basePath.startsWith("classpath:")) {
                this.classpathResource = basePath.substring("classpath:".length());
                Path fsPath = Paths.get(this.classpathResource);
                this.configPath = Files.exists(fsPath) ? fsPath : null;
            } else {
                Path p = Paths.get(basePath);
                this.configPath = Files.isDirectory(p) ? p.resolve(DEFAULT_CONFIG_FILE) : p;
            }
        }

        if (this.configPath == null && this.classpathResource == null) {
            Path cwd = Paths.get(DEFAULT_CONFIG_FILE);
            if (Files.exists(cwd)) {
                this.configPath = cwd;
            } else {
                Path configDir = Paths.get("config", DEFAULT_CONFIG_FILE);
                this.configPath = Files.exists(configDir) ? configDir : null;
            }
        }

        if (this.configPath == null && this.classpathResource == null) {
            this.classpathResource = DEFAULT_CONFIG_FILE;
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized void reload() {
        Map<String, Object> root = null;
        try {
            if (configPath != null && Files.exists(configPath)) {
                root = yaml.load(Files.readString(configPath));
                log.debug("Loaded routes from filesystem: {}", configPath);
            } else if (classpathResource != null) {
                try (InputStream is =
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathResource)) {
                    if (is != null) {
                        root = yaml.load(is);
                        log.debug("Loaded routes from classpath: {}", classpathResource);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load YAML routes", e);
            return;
        }

        if (root == null) {
            log.warn("No gateway-routes.yml found (fsPath={}, classpath={})", configPath, classpathResource);
            routes.set(List.of());
            return;
        }

        List<Map<String, Object>> rawRoutes = (List<Map<String, Object>>) root.get("routes");
        if (rawRoutes == null || rawRoutes.isEmpty()) {
            routes.set(List.of());
            log.info("YamlRouteStore: 0 routes loaded");
            return;
        }

        List<RouteDefinition> parsed = new ArrayList<>();
        for (Map<String, Object> raw : rawRoutes) {
            try {
                parsed.add(parseRoute(raw));
            } catch (Exception e) {
                log.warn("Failed to parse route '{}': {}", raw.get("id"), e.getMessage());
            }
        }

        routes.set(new CopyOnWriteArrayList<>(parsed));
        log.info("YamlRouteStore: {} routes loaded", parsed.size());
    }

    @SuppressWarnings("unchecked")
    private RouteDefinition parseRoute(Map<String, Object> raw) {
        RouteDefinition def = new RouteDefinition();
        def.setId((String) raw.getOrDefault("id", "auto"));
        def.setPath((String) raw.get("path"));
        def.setMethod((String) raw.getOrDefault("method", "POST"));
        def.setEnabled((Boolean) raw.getOrDefault("enabled", true));
        def.setSecurityCode(toStringOrNull(raw.get("securityCode")));
        def.setAuthorize(toStringOrNull(raw.get("authorize")));
        if (raw.get("timeout") instanceof Number n) {
            def.setTimeout(n.longValue());
        }
        if (raw.get("wrapResponse") instanceof Boolean w) {
            def.setWrapResponse(w);
        }

        Map<String, Object> backendRaw = (Map<String, Object>) raw.get("backend");
        if (backendRaw != null) {
            BackendDefinition backend = new BackendDefinition();
            backend.setProtocol((String) backendRaw.get("protocol"));
            backend.setUrl((String) backendRaw.get("url"));
            backend.setBeanName((String) backendRaw.get("beanName"));
            backend.setMethodName((String) backendRaw.get("methodName"));
            def.setBackend(backend);
        }

        List<Map<String, Object>> rf = (List<Map<String, Object>>) raw.get("filters");
        if (rf != null) {
            def.setFilters(parseFilters(rf));
        }
        List<Map<String, Object>> rrf = (List<Map<String, Object>>) raw.get("responseFilters");
        if (rrf != null) {
            def.setResponseFilters(parseFilters(rrf));
        }

        return def;
    }

    /**
     * Converts a raw YAML value to its string form. SnakeYAML parses YAML 1.1 boolean
     * literals ({@code ON/OFF/YES/NO}) into {@link Boolean}, so {@code securityCode: OFF}
     * must be handled explicitly instead of a plain cast.
     */
    private static String toStringOrNull(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private List<FilterDefinition> parseFilters(List<Map<String, Object>> rawList) {
        List<FilterDefinition> result = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            FilterDefinition fd = new FilterDefinition();
            fd.setName((String) raw.get("name"));
            Map<String, Object> props = (Map<String, Object>) raw.get("props");
            if (props != null) {
                fd.setProps(new HashMap<>(props));
            }
            result.add(fd);
        }
        return result;
    }

    private void startFileWatcher() {
        if (configPath == null || !Files.exists(configPath)) {
            return;
        }
        Path dir = configPath.getParent();
        if (dir == null) {
            dir = Paths.get(".");
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();
            dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
            watching = true;
            watchExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "yaml-route-watcher");
                t.setDaemon(true);
                return t;
            });
            watchExecutor.scheduleWithFixedDelay(this::pollWatchEvents, 5, 5, TimeUnit.SECONDS);
            log.info("File watcher started: {}", dir);
        } catch (IOException e) {
            log.warn("File watcher start failed: {}", e.getMessage());
        }
    }

    private void pollWatchEvents() {
        if (!watching) {
            return;
        }
        WatchKey key = watchService.poll();
        if (key == null) {
            return;
        }
        Path configFileName = configPath.getFileName();
        if (configFileName == null) {
            key.reset();
            return;
        }
        for (WatchEvent<?> event : key.pollEvents()) {
            Path changed = (Path) event.context();
            if (changed.endsWith(configFileName)) {
                log.info("Route config changed: {}, reloading...", changed);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
                reload();
                eventPublisher.publishEvent(new RouteStoreRefreshedEvent(this));
            }
        }
        key.reset();
    }
}
