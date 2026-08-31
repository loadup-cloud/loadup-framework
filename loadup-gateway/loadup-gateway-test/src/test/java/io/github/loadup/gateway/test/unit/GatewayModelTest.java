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
package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Gateway Models")
class GatewayModelTest {

    @Nested
    @DisplayName("GatewayRequest builder")
    class GatewayRequestBuilder {

        @Test
        @DisplayName("builds with all fields")
        void buildsAllFields() {
            GatewayRequest req = GatewayRequest.builder()
                    .requestId("req-1")
                    .path("/api/test")
                    .method("POST")
                    .clientIp("192.168.1.1")
                    .headers(Map.of("Content-Type", "application/json"))
                    .body("{\"key\":\"value\"}")
                    .attributes(Map.of("userId", "u1"))
                    .build();

            assertThat(req.getRequestId()).isEqualTo("req-1");
            assertThat(req.getPath()).isEqualTo("/api/test");
            assertThat(req.getMethod()).isEqualTo("POST");
            assertThat(req.getClientIp()).isEqualTo("192.168.1.1");
            assertThat(req.getHeaders()).containsEntry("Content-Type", "application/json");
            assertThat(req.getBody()).isEqualTo("{\"key\":\"value\"}");
            assertThat(req.getAttributes()).containsEntry("userId", "u1");
        }

        @Test
        @DisplayName("headers default")
        void headersDefault() {
            GatewayRequest req = GatewayRequest.builder()
                    .requestId("r")
                    .path("/")
                    .method("GET")
                    .headers(Map.of())
                    .build();
            assertThat(req.getHeaders()).isEmpty();
        }

        @Test
        @DisplayName("attributes default")
        void attributesDefault() {
            GatewayRequest req = GatewayRequest.builder()
                    .requestId("r")
                    .path("/")
                    .method("GET")
                    .attributes(Map.of())
                    .build();
            assertThat(req.getAttributes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GatewayResponse builder")
    class GatewayResponseBuilder {

        @Test
        @DisplayName("builds with all fields")
        void buildsAllFields() {
            LocalDateTime now = LocalDateTime.now();
            GatewayResponse resp = GatewayResponse.builder()
                    .requestId("req-1")
                    .statusCode(200)
                    .headers(Map.of("Content-Type", "text/plain"))
                    .body("response body")
                    .contentType("text/plain")
                    .responseTime(now)
                    .processingTime(100L)
                    .errorMessage(null)
                    .attributes(Map.of("cached", true))
                    .build();

            assertThat(resp.getRequestId()).isEqualTo("req-1");
            assertThat(resp.getStatusCode()).isEqualTo(200);
            assertThat(resp.getBody()).isEqualTo("response body");
            assertThat(resp.getContentType()).isEqualTo("text/plain");
            assertThat(resp.getResponseTime()).isEqualTo(now);
            assertThat(resp.getProcessingTime()).isEqualTo(100L);
            assertThat(resp.getAttributes()).containsEntry("cached", true);
        }

        @Test
        @DisplayName("error response has error message")
        void errorResponse() {
            GatewayResponse resp = GatewayResponse.builder()
                    .requestId("req-1")
                    .statusCode(500)
                    .errorMessage("Internal error")
                    .build();

            assertThat(resp.getStatusCode()).isEqualTo(500);
            assertThat(resp.getErrorMessage()).isEqualTo("Internal error");
        }
    }

    @Nested
    @DisplayName("GatewayContext")
    class GatewayContextTest {

        @Test
        @DisplayName("set and get route")
        void setAndGetRoute() {
            GatewayContext ctx = new GatewayContext();
            RouteConfig route = new RouteConfig();
            route.setPath("/test");
            route.setMethod("GET");
            route.setTarget("http://a");
            ctx.setRoute(route);
            assertThat(ctx.getRoute()).isEqualTo(route);
        }

        @Test
        @DisplayName("set and get request")
        void setAndGetRequest() {
            GatewayContext ctx = new GatewayContext();
            GatewayRequest req = GatewayRequest.builder()
                    .requestId("r")
                    .path("/")
                    .method("GET")
                    .build();
            ctx.setRequest(req);
            assertThat(ctx.getRequest()).isEqualTo(req);
        }

        @Test
        @DisplayName("set and get response")
        void setAndGetResponse() {
            GatewayContext ctx = new GatewayContext();
            GatewayResponse resp =
                    GatewayResponse.builder().requestId("r").statusCode(200).build();
            ctx.setResponse(resp);
            assertThat(ctx.getResponse()).isEqualTo(resp);
        }

        @Test
        @DisplayName("set and get attribute")
        void setAndGetAttribute() {
            GatewayContext ctx = new GatewayContext();
            ctx.setAttribute("key", "value");
            assertThat((String) ctx.getAttribute("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("remove attribute returns null for missing key")
        void removeMissingAttribute() {
            GatewayContext ctx = new GatewayContext();
            assertThat(ctx.getAttribute("missing") == null).isTrue();
        }

        @Test
        @DisplayName("setAttribute overwrites existing")
        void overwriteAttribute() {
            GatewayContext ctx = new GatewayContext();
            ctx.setAttribute("key", "old");
            ctx.setAttribute("key", "new");
            assertThat((String) ctx.getAttribute("key")).isEqualTo("new");
        }
    }
}
