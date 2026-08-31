package io.github.loadup.gateway.facade.event;

import org.springframework.context.ApplicationEvent;

/**
 * Published whenever a {@link io.github.loadup.gateway.facade.spi.RouteStore} reloads its
 * route definitions. The gateway engine listens for this event and recompiles its routing
 * table. Any store implementation (YAML file watch, database poll, config center push)
 * should publish this event after a successful reload.
 */
public class RouteStoreRefreshedEvent extends ApplicationEvent {

    public RouteStoreRefreshedEvent(Object source) {
        super(source);
    }
}
