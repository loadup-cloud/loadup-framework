package io.github.loadup.gateway.test.webmvcapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.loadup.components.configcenter.DefaultConfigCenterTemplate;
import io.github.loadup.components.configcenter.local.LocalConfigCenterProvider;
import io.github.loadup.gateway.api.event.RouteSourceChangedEvent;
import io.github.loadup.gateway.api.model.RouteDocument;
import io.github.loadup.gateway.source.configcenter.ConfigCenterRouteSource;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class ConfigCenterRouteSourceTest {
    @Test
    void publishesChangeAndReadsNewCompleteRevision() {
        DefaultConfigCenterTemplate config = new DefaultConfigCenterTemplate(new LocalConfigCenterProvider());
        AtomicInteger events = new AtomicInteger();
        ApplicationEventPublisher publisher = event -> {
            if (event instanceof RouteSourceChangedEvent) {
                events.incrementAndGet();
            }
        };
        ConfigCenterRouteSource source = new ConfigCenterRouteSource(config, "gateway-routes", 60, publisher);
        source.start();
        try {
            config.setConfig("gateway-routes", document("one"));
            RouteDocument first = source.loadCurrent();
            config.setConfig("gateway-routes", document("two"));
            RouteDocument second = source.loadCurrent();
            assertEquals(2, events.get());
            assertEquals("/api/two", second.routes().getFirst().path());
            assertNotEquals(first.revision(), second.revision());
        } finally {
            source.stop();
        }
    }

    private static String document(String path) {
        return "schemaVersion: 1\nroutes:\n  - id: route\n    order: 1\n    path: /api/" + path
                + "\n    methods: [GET]\n    target: {type: http, uri: https://example.org}\n"
                + "    access: {type: public}\n";
    }
}
