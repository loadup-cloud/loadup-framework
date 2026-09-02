/*-
 * #%L
 * LoadUp Gateway Test
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
package io.github.loadup.gateway.test.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.resilience4j.ResilienceRegistries;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.event.RouteStoreRefreshedEvent;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition.BackendDefinition;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import io.github.loadup.gateway.facade.spi.RouteStore;
import io.github.loadup.gateway.webmvc.exception.GatewayExceptionHandler;
import io.github.loadup.gateway.webmvc.filter.CircuitBreakerHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.RateLimitHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.ResponseWrapperHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.filter.SecurityHandlerFilterFunction;
import io.github.loadup.gateway.webmvc.proxy.ProxyHandlerFunction;
import io.github.loadup.gateway.webmvc.proxy.ProxyProcessorRegistry;
import io.github.loadup.gateway.webmvc.router.RouteFunctionRegistry;
import io.github.loadup.gateway.webmvc.security.OffSecurityStrategy;
import io.github.loadup.gateway.webmvc.security.RouteAuthorizationManager;
import io.github.loadup.gateway.webmvc.security.SecurityStrategyManager;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@DisplayName("RouteFunctionRegistry")
class RouteFunctionRegistryTest {

    @Test
    @DisplayName("compiles enabled routes and matches requests with the route config attached")
    void matchesCompiledRoutes() throws Exception {
        StaticRouteStore store = new StaticRouteStore(List.of(beanRoute("demo", "/api/demo", true)));
        RouteFunctionRegistry registry = newRegistry(store);
        registry.refresh();

        ServerRequest match = WebMvcRequests.request("POST", "/api/demo", "{}");
        Optional<HandlerFunction<ServerResponse>> handler = registry.route(match);
        assertThat(handler).isPresent();
        RouteConfig attached = MvcUtils.getAttribute(match, GatewayAttributes.ROUTE_CONFIG);
        assertThat(attached).isNotNull();
        assertThat(attached.getTargetBean()).isEqualTo("demoService");

        ServerRequest miss = WebMvcRequests.request("GET", "/api/other", null);
        assertThat(registry.route(miss)).isEmpty();
    }

    @Test
    @DisplayName("skips disabled routes")
    void skipsDisabledRoutes() {
        StaticRouteStore store = new StaticRouteStore(List.of(beanRoute("disabled", "/api/disabled", false)));
        RouteFunctionRegistry registry = newRegistry(store);
        registry.refresh();

        ServerRequest request = WebMvcRequests.request("POST", "/api/disabled", null);
        assertThat(registry.route(request)).isEmpty();
    }

    @Test
    @DisplayName("refresh swaps the routing snapshot atomically and reacts to store refresh events")
    void refreshesSnapshot() {
        List<RouteDefinition> routes = new ArrayList<>(List.of(beanRoute("first", "/api/first", true)));
        StaticRouteStore store = new StaticRouteStore(routes);
        RouteFunctionRegistry registry = newRegistry(store);
        registry.refresh();

        assertThat(registry.route(WebMvcRequests.request("POST", "/api/first", null)))
                .isPresent();
        assertThat(registry.route(WebMvcRequests.request("POST", "/api/second", null)))
                .isEmpty();

        routes.clear();
        routes.add(beanRoute("second", "/api/second", true));
        registry.onRouteStoreRefreshed(new RouteStoreRefreshedEvent(store));

        assertThat(registry.route(WebMvcRequests.request("POST", "/api/first", null)))
                .isEmpty();
        assertThat(registry.route(WebMvcRequests.request("POST", "/api/second", null)))
                .isPresent();
    }

    @Test
    @DisplayName("fails startup when route definitions are invalid")
    void failsStartupForInvalidRoutes() {
        RouteDefinition invalid = beanRoute("invalid", "missing-leading-slash", true);
        RouteFunctionRegistry registry = newRegistry(new StaticRouteStore(List.of(invalid)));

        assertThatThrownBy(registry::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to initialize gateway routes")
                .hasRootCauseMessage("Invalid gateway routes:\n - invalid: path must start with '/'");
    }

    @Test
    @DisplayName("keeps the previous snapshot when a runtime refresh is invalid")
    void keepsPreviousSnapshotForInvalidRuntimeRefresh() {
        List<RouteDefinition> routes = new ArrayList<>(List.of(beanRoute("valid", "/api/valid", true)));
        StaticRouteStore store = new StaticRouteStore(routes);
        RouteFunctionRegistry registry = newRegistry(store);
        registry.init();

        routes.clear();
        routes.add(beanRoute("invalid", "missing-leading-slash", true));
        registry.refresh();

        assertThat(registry.route(WebMvcRequests.request("POST", "/api/valid", null)))
                .isPresent();
    }

    @Test
    @DisplayName("executes the full pipeline through the proxy handler")
    void executesPipeline() throws Exception {
        StaticRouteStore store = new StaticRouteStore(List.of(beanRoute("demo", "/api/demo", true)));
        RouteFunctionRegistry registry = newRegistry(store);
        registry.refresh();

        ServerRequest request = WebMvcRequests.request("POST", "/api/demo", "{\"x\":1}");
        Optional<HandlerFunction<ServerResponse>> handler = registry.route(request);
        assertThat(handler).isPresent();

        ServerResponse response = handler.get().handle(request);
        assertThat(response.statusCode().value()).isEqualTo(200);
        GatewayResponse backend = MvcUtils.getAttribute(request, GatewayAttributes.PROXY_RESPONSE);
        assertThat(backend).isNotNull();
        assertThat(backend.getBody()).isEqualTo("{\"ok\":true}");
    }

    @Test
    @DisplayName("opens the Resilience4j circuit breaker after repeated 5xx responses")
    void opensCircuitBreakerAfterFailures() throws Exception {
        RouteDefinition definition = beanRoute("failing", "/api/failing", true);
        definition.getProperties().put("circuitBreaker.enabled", true);
        definition.getProperties().put("circuitBreaker.minimumNumberOfCalls", 2);
        definition.getProperties().put("circuitBreaker.failureRateThreshold", 50);
        definition.getProperties().put("circuitBreaker.slidingWindowSize", 4);
        definition.getProperties().put("circuitBreaker.waitDurationInOpenState", 5);
        StaticRouteStore store = new StaticRouteStore(List.of(definition));
        RouteFunctionRegistry registry = newRegistry(store, new FailingProxyProcessor());
        registry.refresh();

        ServerRequest request = WebMvcRequests.request("POST", "/api/failing", "{}");
        Optional<HandlerFunction<ServerResponse>> handler = registry.route(request);
        assertThat(handler).isPresent();
        assertThat(handler.get().handle(request).statusCode().value()).isEqualTo(500);
        assertThat(handler.get().handle(request).statusCode().value()).isEqualTo(500);

        ServerResponse shortCircuited = handler.get().handle(request);
        assertThat(shortCircuited.statusCode().value()).isEqualTo(503);
    }

    @Test
    @DisplayName("rejects requests once the rate limit capacity is exhausted")
    void rejectsWhenRateLimitExceeded() throws Exception {
        RouteDefinition definition = beanRoute("limited", "/api/limited", true);
        definition.getProperties().put("rateLimit.enabled", true);
        definition.getProperties().put("rateLimit.capacity", 1);
        definition.getProperties().put("rateLimit.refillRate", 1);
        StaticRouteStore store = new StaticRouteStore(List.of(definition));
        RouteFunctionRegistry registry = newRegistry(store);
        registry.refresh();

        ServerRequest request = WebMvcRequests.request("POST", "/api/limited", "{}");
        Optional<HandlerFunction<ServerResponse>> handler = registry.route(request);
        assertThat(handler).isPresent();
        assertThat(handler.get().handle(request).statusCode().value()).isEqualTo(200);

        assertThat(handler.get().handle(request).statusCode().value()).isEqualTo(429);
    }

    private static RouteFunctionRegistry newRegistry(RouteStore store) {
        return newRegistry(store, new StubProxyProcessor());
    }

    private static RouteFunctionRegistry newRegistry(RouteStore store, ProxyProcessor proxyProcessor) {
        GatewayProperties properties = new GatewayProperties();
        ProxyHandlerFunction proxyHandler =
                new ProxyHandlerFunction(new ProxyProcessorRegistry(List.of(proxyProcessor)));
        return new RouteFunctionRegistry(
                store,
                properties,
                proxyHandler,
                new GatewayExceptionHandler(),
                null,
                new SecurityHandlerFilterFunction(
                        new SecurityStrategyManager(List.of(new OffSecurityStrategy())),
                        new RouteAuthorizationManager()),
                new RateLimitHandlerFilterFunction(ResilienceRegistries.ofDefaults()),
                new CircuitBreakerHandlerFilterFunction(ResilienceRegistries.ofDefaults()),
                new ResponseWrapperHandlerFilterFunction(properties));
    }

    private static final class FailingProxyProcessor implements ProxyProcessor {
        @Override
        public String getName() {
            return "failing";
        }

        @Override
        public String getType() {
            return "PROXY";
        }

        @Override
        public String getVersion() {
            return "test";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public void initialize() {}

        @Override
        public void destroy() {}

        @Override
        public String getSupportedProtocol() {
            return "BEAN";
        }

        @Override
        public GatewayResponse proxy(GatewayRequest request, RouteConfig route) {
            return GatewayResponse.builder()
                    .requestId(request.getRequestId())
                    .statusCode(500)
                    .headers(Map.of())
                    .body("{\"error\":\"boom\"}")
                    .contentType("application/json")
                    .responseTime(LocalDateTime.now())
                    .build();
        }
    }

    private static RouteDefinition beanRoute(String id, String path, boolean enabled) {
        RouteDefinition def = new RouteDefinition();
        def.setId(id);
        def.setPath(path);
        def.setMethod("POST");
        def.setEnabled(enabled);
        BackendDefinition backend = new BackendDefinition();
        backend.setProtocol("bean");
        backend.setBeanName("demoService");
        backend.setMethodName("hello");
        def.setBackend(backend);
        return def;
    }

    private static final class StaticRouteStore implements RouteStore {
        private volatile List<RouteDefinition> routes;

        private StaticRouteStore(List<RouteDefinition> routes) {
            this.routes = routes;
        }

        @Override
        public List<RouteDefinition> loadAll() {
            return routes;
        }

        @Override
        public Optional<RouteDefinition> load(String routeId) {
            return routes.stream().filter(r -> r.getId().equals(routeId)).findFirst();
        }
    }

    private static final class StubProxyProcessor implements ProxyProcessor {
        @Override
        public String getName() {
            return "stub";
        }

        @Override
        public String getType() {
            return "PROXY";
        }

        @Override
        public String getVersion() {
            return "test";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public void initialize() {}

        @Override
        public void destroy() {}

        @Override
        public String getSupportedProtocol() {
            return "BEAN";
        }

        @Override
        public GatewayResponse proxy(GatewayRequest request, RouteConfig route) {
            return GatewayResponse.builder()
                    .requestId(request.getRequestId())
                    .statusCode(200)
                    .headers(Map.of())
                    .body("{\"ok\":true}")
                    .contentType("application/json")
                    .responseTime(LocalDateTime.now())
                    .build();
        }
    }
}
