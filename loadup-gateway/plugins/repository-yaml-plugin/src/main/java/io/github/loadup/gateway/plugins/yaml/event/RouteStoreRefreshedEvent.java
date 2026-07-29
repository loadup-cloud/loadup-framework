package io.github.loadup.gateway.plugins.yaml.event;

import org.springframework.context.ApplicationEvent;

/**
 * Published when the YAML route store detects a file change and reloads routes.
 * Listeners (e.g. DefaultGatewayEngine) can refresh their caches accordingly.
 */
public class RouteStoreRefreshedEvent extends ApplicationEvent {

    public RouteStoreRefreshedEvent(Object source) {
        super(source);
    }
}
