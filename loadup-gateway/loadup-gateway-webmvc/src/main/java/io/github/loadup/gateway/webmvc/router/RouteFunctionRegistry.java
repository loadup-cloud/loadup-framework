/*-
 * #%L
 * Loadup Gateway WebMVC Engine
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
package io.github.loadup.gateway.webmvc.router;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.event.RouteStoreRefreshedEvent;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.spi.RouteStore;
import io.github.loadup.gateway.webmvc.exception.GatewayExceptionHandler;
import io.github.loadup.gateway.webmvc.filter.CircuitBreakerHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.RateLimitHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.ResponseWrapperHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.SecurityHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.TracingHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.proxy.ProxyHandlerFunction;
import io.github.loadup.gateway.webmvc.support.RouteConfigConverter;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Spring MVC entry point of the gateway engine.
 *
 * <p>Exposed to the container as a single {@link RouterFunction} bean. The active routing
 * table is an immutable snapshot stored in an {@link AtomicReference}; refreshing the
 * registry compiles a brand new snapshot and swaps it atomically, so in-flight requests
 * keep the previous table and new requests immediately see the new one.
 *
 * <p>Refreshes are triggered at startup and whenever the route store publishes a
 * {@link RouteStoreRefreshedEvent} (YAML file watch, DB refresh, admin API, ...).
 */
public class RouteFunctionRegistry implements RouterFunction<ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(RouteFunctionRegistry.class);

    private final RouteStore routeStore;
    private final GatewayProperties properties;
    private final ProxyHandlerFunction proxyHandler;
    private final GatewayExceptionHandler exceptionHandler;
    private final TracingHandlerFilterFunction tracingFilter;
    private final SecurityHandlerFilterFunction securityFilter;
    private final RateLimitHandlerFilterFunction rateLimitFilter;
    private final CircuitBreakerHandlerFilterFunction circuitBreakerFilter;
    private final ResponseWrapperHandlerFilterFunction responseWrapperFilter;

    private final AtomicReference<RouterFunction<ServerResponse>> snapshot =
            new AtomicReference<>(request -> Optional.empty());
    private final AtomicLong revision = new AtomicLong();

    public RouteFunctionRegistry(
            RouteStore routeStore,
            GatewayProperties properties,
            ProxyHandlerFunction proxyHandler,
            GatewayExceptionHandler exceptionHandler,
            TracingHandlerFilterFunction tracingFilter,
            SecurityHandlerFilterFunction securityFilter,
            RateLimitHandlerFilterFunction rateLimitFilter,
            CircuitBreakerHandlerFilterFunction circuitBreakerFilter,
            ResponseWrapperHandlerFilterFunction responseWrapperFilter) {
        this.routeStore = routeStore;
        this.properties = properties;
        this.proxyHandler = proxyHandler;
        this.exceptionHandler = exceptionHandler;
        this.tracingFilter = tracingFilter;
        this.securityFilter = securityFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.circuitBreakerFilter = circuitBreakerFilter;
        this.responseWrapperFilter = responseWrapperFilter;
    }

    @PostConstruct
    public void init() {
        refresh(true);
    }

    /**
     * Recompile all enabled routes from the route store and atomically publish the new
     * snapshot. On failure the previous snapshot is kept.
     */
    public void refresh() {
        refresh(false);
    }

    private void refresh(boolean failFast) {
        try {
            List<RouteDefinition> definitions = routeStore.loadAll();
            RouteDefinitionValidator.validate(definitions, proxyHandler::supportsProtocol);
            List<RouteConfig> routes = definitions.stream()
                    .filter(RouteDefinition::isEnabled)
                    .map(definition -> RouteConfigConverter.convert(definition, properties))
                    .toList();
            if (circuitBreakerFilter != null) {
                Set<String> activeKeys = routes.stream()
                        .map(CircuitBreakerHandlerFilterFunction::keyOf)
                        .collect(Collectors.toSet());
                circuitBreakerFilter.prune(activeKeys);
            }
            RouterFunction<ServerResponse> next = RouteFunctionCompiler.compile(
                    routes,
                    proxyHandler,
                    exceptionHandler,
                    tracingFilter,
                    securityFilter,
                    rateLimitFilter,
                    circuitBreakerFilter,
                    responseWrapperFilter);
            snapshot.set(next);
            long current = revision.incrementAndGet();
            log.info("Gateway routes refreshed: revision={}, enabled={}", current, routes.size());
        } catch (Exception e) {
            if (failFast) {
                throw new IllegalStateException("Failed to initialize gateway routes", e);
            }
            log.error("Failed to refresh gateway routes, keeping previous snapshot", e);
        }
    }

    @EventListener
    public void onRouteStoreRefreshed(RouteStoreRefreshedEvent event) {
        log.info("Route store refreshed event received, reloading routes");
        refresh();
    }

    @Override
    public Optional<HandlerFunction<ServerResponse>> route(ServerRequest request) {
        return snapshot.get().route(request);
    }

    public long getRevision() {
        return revision.get();
    }
}
