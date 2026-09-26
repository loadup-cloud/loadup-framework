package io.github.loadup.gateway.plugins.yaml;

import io.github.loadup.gateway.api.event.RouteSourceChangedEvent;
import io.github.loadup.gateway.api.model.ManagedRoute;
import io.github.loadup.gateway.api.model.RouteDocument;
import io.github.loadup.gateway.api.spi.RouteSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.context.ApplicationEventPublisher;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Reads complete versioned route documents and checks external files for changes. */
public final class YamlRouteSource implements RouteSource {
    private final String location;
    private final int pollSeconds;
    private final ApplicationEventPublisher publisher;
    private ScheduledExecutorService poller;
    private WatchService watcher;

    public YamlRouteSource(String location, int pollSeconds, ApplicationEventPublisher publisher) {
        this.location = location;
        this.pollSeconds = Math.max(1, pollSeconds);
        this.publisher = publisher;
    }

    @PostConstruct
    public void start() {
        if (location.startsWith("classpath:")) {
            return;
        }
        poller = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "gateway-route-source");
            thread.setDaemon(true);
            return thread;
        });
        poller.scheduleWithFixedDelay(this::checkForChange, pollSeconds, pollSeconds, TimeUnit.SECONDS);
        try {
            Path file = Path.of(location).toAbsolutePath();
            watcher = FileSystems.getDefault().newWatchService();
            file.getParent()
                    .register(
                            watcher,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_DELETE);
            poller.scheduleWithFixedDelay(() -> checkWatch(file.getFileName()), 1, 1, TimeUnit.SECONDS);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot watch route source: " + location, e);
        }
    }

    @PreDestroy
    public void stop() {
        if (poller != null) {
            poller.shutdownNow();
        }
        if (watcher != null) {
            try {
                watcher.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void checkWatch(Path fileName) {
        WatchKey key;
        while ((key = watcher.poll()) != null) {
            boolean changed = key.pollEvents().stream().anyMatch(event -> fileName.equals(event.context()));
            key.reset();
            if (changed) {
                checkForChange();
            }
        }
    }

    private void checkForChange() {
        publisher.publishEvent(new RouteSourceChangedEvent(this));
    }

    @Override
    public RouteDocument loadCurrent() {
        return parse(location, readBytes());
    }

    /** Parses one complete snapshot; shared by external file and config-center sources. */
    public static RouteDocument parse(String sourceId, byte[] bytes) {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        Object parsed =
                new Yaml(new SafeConstructor(options)).load(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
        Map<?, ?> root = map(parsed, "document");
        onlyKeys(root, "document", Set.of("schemaVersion", "routes"));
        int schemaVersion = integer(root.get("schemaVersion"), "schemaVersion");
        List<?> rawRoutes = list(root.get("routes"), "routes");
        List<ManagedRoute> routes = new ArrayList<>();
        for (Object raw : rawRoutes) {
            Map<?, ?> route = map(raw, "route");
            onlyKeys(route, "route", Set.of("id", "order", "path", "methods", "target", "access", "filters"));
            Map<?, ?> target = map(route.get("target"), "target");
            onlyKeys(target, "target", Set.of("type", "bean", "method", "uri"));
            Map<?, ?> access = map(route.get("access"), "access");
            onlyKeys(access, "access", Set.of("type", "anyOf", "signature"));
            List<ManagedRoute.Filter> filters = new ArrayList<>();
            if (route.containsKey("filters")) {
                for (Object item : list(route.get("filters"), "filters")) {
                    Map<?, ?> filter = map(item, "filter");
                    onlyKeys(filter, "filter", Set.of("name", "args"));
                    Map<?, ?> args = filter.containsKey("args") ? map(filter.get("args"), "args") : Map.of();
                    java.util.Map<String, String> values = new java.util.HashMap<>();
                    args.forEach((key, value) -> values.put(String.valueOf(key), String.valueOf(value)));
                    filters.add(new ManagedRoute.Filter(string(filter.get("name")), values));
                }
            }
            routes.add(new ManagedRoute(
                    string(route.get("id")),
                    integer(route.get("order"), "order"),
                    string(route.get("path")),
                    list(route.get("methods"), "methods").stream()
                            .map(YamlRouteSource::string)
                            .toList(),
                    new ManagedRoute.Target(
                            string(target.get("type")),
                            string(target.get("bean")),
                            string(target.get("method")),
                            string(target.get("uri"))),
                    new ManagedRoute.Access(
                            string(access.get("type")),
                            access.containsKey("anyOf")
                                    ? list(access.get("anyOf"), "anyOf").stream()
                                            .map(YamlRouteSource::string)
                                            .toList()
                                    : List.of(),
                            bool(
                                    access.containsKey("signature") ? access.get("signature") : Boolean.FALSE,
                                    "access.signature")),
                    filters));
        }
        String revision = digest(bytes);
        return new RouteDocument(sourceId, revision, schemaVersion, routes);
    }

    private byte[] readBytes() {
        try {
            if (location.startsWith("classpath:")) {
                String name = location.substring("classpath:".length());
                try (InputStream stream =
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
                    if (stream == null) {
                        throw new IllegalStateException("Route resource not found: " + location);
                    }
                    return stream.readAllBytes();
                }
            }
            return Files.readAllBytes(Path.of(location));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read route source: " + location, e);
        }
    }

    private static String digest(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<?, ?> map(Object value, String field) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }
        throw new IllegalArgumentException(field + " must be a mapping");
    }

    private static List<?> list(Object value, String field) {
        if (value instanceof List<?> list) {
            return list;
        }
        throw new IllegalArgumentException(field + " must be a list");
    }

    private static int integer(Object value, String field) {
        if (value instanceof Integer number) {
            return number;
        }
        if (value instanceof Long number) {
            return Math.toIntExact(number);
        }
        throw new IllegalArgumentException(field + " must be an integer");
    }

    private static String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static boolean bool(Object value, String field) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        throw new IllegalArgumentException(field + " must be a boolean");
    }

    private static void onlyKeys(Map<?, ?> values, String field, Set<String> allowed) {
        for (Object key : values.keySet()) {
            if (!(key instanceof String name) || !allowed.contains(name)) {
                throw new IllegalArgumentException("Unknown " + field + " field: " + key);
            }
        }
    }
}
