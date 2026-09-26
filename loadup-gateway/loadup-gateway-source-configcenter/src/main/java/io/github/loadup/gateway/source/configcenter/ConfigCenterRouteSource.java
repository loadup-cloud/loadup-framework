package io.github.loadup.gateway.source.configcenter;

import io.github.loadup.components.configcenter.ConfigCenterTemplate;
import io.github.loadup.gateway.api.event.RouteSourceChangedEvent;
import io.github.loadup.gateway.api.model.RouteDocument;
import io.github.loadup.gateway.api.spi.RouteSource;
import io.github.loadup.gateway.plugins.yaml.YamlRouteSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.springframework.context.ApplicationEventPublisher;

/** Reads the published YAML document from the selected ConfigCenter binder. */
public final class ConfigCenterRouteSource implements RouteSource {
    private final ConfigCenterTemplate config;
    private final String key;
    private final int pollSeconds;
    private final ApplicationEventPublisher events;
    private final Consumer<String> listener;
    private ScheduledExecutorService poller;

    public ConfigCenterRouteSource(
            ConfigCenterTemplate config, String key, int pollSeconds, ApplicationEventPublisher events) {
        this.config = config;
        this.key = key;
        this.pollSeconds = Math.max(1, pollSeconds);
        this.events = events;
        this.listener = ignored -> events.publishEvent(new RouteSourceChangedEvent(this));
    }

    @PostConstruct
    public void start() {
        config.addListener(key, listener);
        poller = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "gateway-configcenter-source");
            thread.setDaemon(true);
            return thread;
        });
        poller.scheduleWithFixedDelay(
                () -> events.publishEvent(new RouteSourceChangedEvent(this)),
                pollSeconds,
                pollSeconds,
                TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        config.removeListener(key);
        if (poller != null) {
            poller.shutdownNow();
        }
    }

    @Override
    public RouteDocument loadCurrent() {
        String value = config.getConfig(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("No gateway route document at config key: " + key);
        }
        return YamlRouteSource.parse("configcenter:" + key, value.getBytes(StandardCharsets.UTF_8));
    }
}
