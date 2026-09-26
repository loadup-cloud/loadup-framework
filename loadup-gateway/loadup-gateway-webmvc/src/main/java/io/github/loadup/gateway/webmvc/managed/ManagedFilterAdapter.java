package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.model.ManagedRoute;
import org.springframework.web.servlet.function.RouterFunctions;

/** Validates and installs an allowed declarative SCG MVC filter. */
public interface ManagedFilterAdapter {
    String name();

    void configure(RouterFunctions.Builder builder, ManagedRoute route, ManagedRoute.Filter filter);
}
