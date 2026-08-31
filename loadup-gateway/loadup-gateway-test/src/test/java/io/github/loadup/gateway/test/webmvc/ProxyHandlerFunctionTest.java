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

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition.BackendDefinition;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import io.github.loadup.gateway.webmvc.proxy.ProxyHandlerFunction;
import io.github.loadup.gateway.webmvc.proxy.ProxyProcessorRegistry;
import io.github.loadup.gateway.webmvc.support.GatewayAttributes;
import io.github.loadup.gateway.webmvc.support.RouteConfigConverter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@DisplayName("ProxyHandlerFunction")
class ProxyHandlerFunctionTest {

    @Test
    @DisplayName("dispatches to the registered proxy processor and exposes the backend response")
    void dispatchesToProcessor() throws Exception {
        ProxyProcessor processor = new StubProxyProcessor("{\"ok\":true}");
        ProxyHandlerFunction handler = new ProxyHandlerFunction(new ProxyProcessorRegistry(List.of(processor)));

        GatewayProperties properties = new GatewayProperties();
        RouteConfig route = RouteConfigConverter.convert(beanRoute("demoService", "hello"), properties);
        ServerRequest request = WebMvcRequests.request("POST", "/api/demo", "{\"name\":\"x\"}");
        MvcUtils.putAttribute(request, GatewayAttributes.ROUTE_CONFIG, route);

        ServerResponse response = handler.handle(request);

        assertThat(response.statusCode().value()).isEqualTo(200);
        GatewayResponse backend = MvcUtils.getAttribute(request, GatewayAttributes.PROXY_RESPONSE);
        assertThat(backend).isNotNull();
        assertThat(backend.getBody()).isEqualTo("{\"ok\":true}");
        assertThat(backend.getProcessingTime()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("fails when no processor is registered for the route protocol")
    void failsOnMissingProcessor() {
        ProxyHandlerFunction handler = new ProxyHandlerFunction(new ProxyProcessorRegistry(List.of()));
        GatewayProperties properties = new GatewayProperties();
        RouteConfig route = RouteConfigConverter.convert(beanRoute("demoService", "hello"), properties);
        ServerRequest request = WebMvcRequests.request("POST", "/api/demo", null);
        MvcUtils.putAttribute(request, GatewayAttributes.ROUTE_CONFIG, route);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> handler.handle(request));
    }

    private static RouteDefinition beanRoute(String beanName, String methodName) {
        RouteDefinition def = new RouteDefinition();
        def.setId("demo");
        def.setPath("/api/demo");
        def.setMethod("POST");
        BackendDefinition backend = new BackendDefinition();
        backend.setProtocol("bean");
        backend.setBeanName(beanName);
        backend.setMethodName(methodName);
        def.setBackend(backend);
        return def;
    }

    private static final class StubProxyProcessor implements ProxyProcessor {
        private final String body;

        private StubProxyProcessor(String body) {
            this.body = body;
        }

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
                    .headers(Map.of("X-Backend", "stub"))
                    .body(body)
                    .contentType("application/json")
                    .responseTime(LocalDateTime.now())
                    .build();
        }
    }
}
