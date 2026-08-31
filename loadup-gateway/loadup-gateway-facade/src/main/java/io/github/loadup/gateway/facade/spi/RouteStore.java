package io.github.loadup.gateway.facade.spi;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
