package io.github.loadup.gateway.facade.spi;

import io.github.loadup.gateway.facade.model.RouteDefinition;
import java.util.List;
import java.util.Optional;

/**
 * SPI for loading route definitions from a configuration source.
 *
 * <p>Implementations include YAML file, JDBC database, Nacos config center, etc.
 * The store is responsible for loading route definitions; the core engine
 * compiles them into executable filter chains.
 *
 * <p>Implementations SHOULD support hot reload by firing
 * {@link org.springframework.context.ApplicationEvent} on change.
 */
public interface RouteStore {

    /**
     * Load all enabled route definitions.
     */
    List<RouteDefinition> loadAll();

    /**
     * Load a single route definition by ID.
     */
    Optional<RouteDefinition> load(String routeId);

    /**
     * Save (create or update) a route definition. Used by the admin API.
     * Not all stores support writes — they may throw UnsupportedOperationException.
     */
    default RouteDefinition save(RouteDefinition definition) {
        throw new UnsupportedOperationException("This RouteStore does not support writes");
    }

    /**
     * Delete a route definition by ID. Used by the admin API.
     */
    default void delete(String routeId) {
        throw new UnsupportedOperationException("This RouteStore does not support writes");
    }
}
