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

import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RouteConfig")
class RouteConfigTest {

    private RouteConfig buildRoute(String path, String method, String target) {
        return buildRoute(path, method, target, null, null);
    }

    private RouteConfig buildRoute(
            String path, String method, String target, Map<String, Object> properties, Boolean enabled) {
        RouteConfig config = new RouteConfig();
        config.setPath(path);
        config.setMethod(method);
        config.setTarget(target);
        if (properties != null) {
            config.setProperties(properties);
        }
        if (enabled != null) {
            config.setEnabled(enabled);
        }
        return config;
    }

    @Nested
    @DisplayName("properties")
    class PropertiesTest {

        @Test
        @DisplayName("properties set and retrieved")
        void propertiesSetRetrieved() {
            Map<String, Object> props = new HashMap<>();
            props.put("retryCount", 3);
            RouteConfig config = buildRoute("/api/test", "GET", "http://localhost/api", props, null);
            assertThat(config.getProperties()).containsEntry("retryCount", 3);
        }
    }

    @Nested
    @DisplayName("basic getters")
    class BasicGetters {

        @Test
        @DisplayName("path and method are set")
        void pathAndMethod() {
            RouteConfig config = buildRoute("/api/test", "POST", "http://a");
            assertThat(config.getPath()).isEqualTo("/api/test");
            assertThat(config.getMethod()).isEqualTo("POST");
        }

        @Test
        @DisplayName("target is set")
        void targetIsSet() {
            RouteConfig config = buildRoute("/api/test", "GET", "http://localhost:8080/api");
            assertThat(config.getTarget()).isEqualTo("http://localhost:8080/api");
        }
    }

    @Nested
    @DisplayName("enabled flag")
    class Enabled {

        @Test
        @DisplayName("defaults to false")
        void defaultsToFalse() {
            RouteConfig config = buildRoute("/api/test", "GET", "http://a");
            assertThat(config.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("explicitly enabled")
        void explicitlyEnabled() {
            RouteConfig config = buildRoute("/api/test", "GET", "http://a", null, true);
            assertThat(config.isEnabled()).isTrue();
        }
    }
}
