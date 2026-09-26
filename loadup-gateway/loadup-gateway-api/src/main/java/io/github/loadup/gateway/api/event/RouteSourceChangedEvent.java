package io.github.loadup.gateway.api.event;

import org.springframework.context.ApplicationEvent;

/** A source hint; consumers must reload its complete current document. */
public final class RouteSourceChangedEvent extends ApplicationEvent {
    public RouteSourceChangedEvent(Object source) {
        super(source);
    }
}
