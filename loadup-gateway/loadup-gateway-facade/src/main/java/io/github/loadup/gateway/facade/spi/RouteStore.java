package io.github.loadup.gateway.facade.spi;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
