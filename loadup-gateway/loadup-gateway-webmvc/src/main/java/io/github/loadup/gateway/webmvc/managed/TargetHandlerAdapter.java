package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.model.ManagedRoute;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/** Validates and compiles one managed target type without runtime dispatch. */
public interface TargetHandlerAdapter {
    String type();

    HandlerFunction<ServerResponse> compile(ManagedRoute route);

    default void configure(RouterFunctions.Builder builder, ManagedRoute route) {}

    default void configureAfterFilters(RouterFunctions.Builder builder, ManagedRoute route) {}
}
