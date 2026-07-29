package io.github.loadup.gateway.plugins.yaml;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.model.FilterDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition.BackendDefinition;
import io.github.loadup.gateway.facade.spi.RouteStore;
import io.github.loadup.gateway.plugins.yaml.event.RouteStoreRefreshedEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.yaml.snakeyaml.Yaml;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * YAML file route store with file-watcher hot reload.
 *
 * <p>Looks for {@code gateway-routes.yml} on the classpath or filesystem.
 * Hot reload via Java {@link WatchService} — edit the YAML file and
 * routes update within seconds, no restart needed.
 *
 * <p>YAML format:
 * <pre>
 * routes:
 *   - id: user-api
 *     path: /api/users
 *     method: POST
 *     backend:
 *       protocol: http
 *       url: http://user-service:8080/users
 *     filters:
 *       - name: rate-limit
 *         props:
 *           capacity: 100
 *       - name: security
 *   - id: health-check
 *     path: /api/health
 *     method: GET
 *     backend:
 *       protocol: bean
 *       beanName: healthService
 *       methodName: check
 *     filters: []
 *     wrapResponse: false
 * </pre>
 */
public class YamlRouteStore implements RouteStore {
    private static final Logger log = LoggerFactory.getLogger(YamlRouteStore.class);


    private static final String DEFAULT_CONFIG_FILE = "gateway-routes.yml";

    private final GatewayProperties gatewayProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final Yaml yaml = new Yaml();
    private final AtomicReference<List<RouteDefinition>> routes = new AtomicReference<>(List.of());

    private Path configPath;
    private WatchService watchService;
    private ScheduledExecutorService watchExecutor;
    private volatile boolean watching = false;

    public YamlRouteStore(GatewayProperties gatewayProperties, ApplicationEventPublisher eventPublisher) {
        this.gatewayProperties = gatewayProperties;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        configPath = resolveConfigPath();
        log.info("YamlRouteStore loading from: {}", configPath.toAbsolutePath());
        reload();
        startFileWatcher();
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
        return routes.get().stream()
            .filter(r -> r.getId().equals(routeId))
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    private synchronized void reload() {
        try {
            Map<String, Object> root;
            if (Files.exists(configPath)) {
                String content = Files.readString(configPath);
                root = yaml.load(content);
            } else {
                // Try classpath fallback
                try (InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(DEFAULT_CONFIG_FILE)) {
                    if (is == null) {
                        log.warn("No gateway-routes.yml found on filesystem or classpath");
                        routes.set(List.of());
                        return;
                    }
                    root = yaml.load(is);
                }
            }

            if (root == null) {
                routes.set(List.of());
                return;
            }

            List<Map<String, Object>> rawRoutes = (List<Map<String, Object>>) root.get("routes");
            if (rawRoutes == null || rawRoutes.isEmpty()) {
                routes.set(List.of());
                log.info("YamlRouteStore loaded 0 routes");
                return;
            }

            List<RouteDefinition> parsed = new ArrayList<>();
            for (Map<String, Object> raw : rawRoutes) {
                try {
                    parsed.add(parseRoute(raw));
                } catch (Exception e) {
                    log.warn("Failed to parse route: {}", raw.get("id"), e);
                }
            }

            routes.set(new CopyOnWriteArrayList<>(parsed));
            log.info("YamlRouteStore loaded {} routes", parsed.size());
        } catch (Exception e) {
            log.error("Failed to load YAML routes from {}", configPath, e);
        }
    }

    @SuppressWarnings("unchecked")
    private RouteDefinition parseRoute(Map<String, Object> raw) {
        RouteDefinition routeDefinition = new RouteDefinition();

        routeDefinition.setId((String) raw.getOrDefault("id", "auto"));
        routeDefinition.setPath((String) raw.get("path"));
        routeDefinition.setMethod((String) raw.getOrDefault("method", "POST"));
        routeDefinition.setEnabled((Boolean) raw.getOrDefault("enabled", true));
        routeDefinition.setSecurityCode((String) raw.get("securityCode"));

        if (raw.get("timeout") instanceof Number n) routeDefinition.setTimeout(n.longValue());
        if (raw.get("wrapResponse") instanceof Boolean b) routeDefinition.setWrapResponse(b);

        // Parse backend
        Map<String, Object> backendRaw = (Map<String, Object>) raw.get("backend");
        if (backendRaw != null) {
            BackendDefinition backendDefinition = new BackendDefinition();
            backendDefinition.setProtocol((String) backendRaw.get("protocol"));
            backendDefinition.setUrl((String) backendRaw.get("url"));
            backendDefinition.setBeanName((String) backendRaw.get("beanName"));
            backendDefinition.setMethodName((String) backendRaw.get("methodName"));
            routeDefinition.setBackend(backendDefinition);
        }

        // Parse filters
        List<Map<String, Object>> rawFilters = (List<Map<String, Object>>) raw.get("filters");
        if (rawFilters != null) {
            routeDefinition.setFilters(parseFilters(rawFilters));
        }

        // Parse response filters
        List<Map<String, Object>> rawRespFilters = (List<Map<String, Object>>) raw.get("responseFilters");
        if (rawRespFilters != null) {
            routeDefinition.setResponseFilters(parseFilters(rawRespFilters));
        }

        return routeDefinition;
    }

    @SuppressWarnings("unchecked")
    private List<FilterDefinition> parseFilters(List<Map<String, Object>> rawList) {
        List<FilterDefinition> result = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            FilterDefinition fb = new FilterDefinition();
            fb.setName((String) raw.get("name"));
            Map<String, Object> props = (Map<String, Object>) raw.get("props");
            if (props != null) {
                fb.setProps(new java.util.HashMap<>(props));
            }
            result.add(fb);
        }
        return result;
    }

    private Path resolveConfigPath() {
        // Check GatewayProperties for explicit path
        if (gatewayProperties.getStorage() != null
            && gatewayProperties.getStorage().getFile() != null
            && gatewayProperties.getStorage().getFile().getBasePath() != null) {
            String basePath = gatewayProperties.getStorage().getFile().getBasePath();
            if (!basePath.startsWith("classpath:")) {
                return Paths.get(basePath, DEFAULT_CONFIG_FILE);
            }
        }
        // Default: look in current working directory, then classpath
        Path cwd = Paths.get(DEFAULT_CONFIG_FILE);
        if (Files.exists(cwd)) return cwd;
        return Paths.get("config", DEFAULT_CONFIG_FILE);
    }

    private void startFileWatcher() {
        if (!Files.exists(configPath)) return;

        Path dir = configPath.getParent();
        if (dir == null) dir = Paths.get(".");

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
            log.info("File watcher started for directory: {}", dir.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to start file watcher, hot reload disabled: {}", e.getMessage());
        }
    }

    private void pollWatchEvents() {
        if (!watching) return;
        WatchKey key = watchService.poll();
        if (key == null) return;

        for (WatchEvent<?> event : key.pollEvents()) {
            Path changed = (Path) event.context();
            if (changed.endsWith(configPath.getFileName().toString())) {
                log.info("Route config changed: {}, reloading...", changed);
                // Debounce: wait a moment for file write to complete
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
