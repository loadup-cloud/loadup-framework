package io.github.loadup.gateway.test.unit;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.webmvc.security.NimbusResourceServerBinder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NimbusResourceServerBinder")
class NimbusResourceServerBinderTest {

    @Test
    @DisplayName("uses the shared HS256 secret by default")
    void defaultsToSharedSecret() {
        GatewayProperties properties = new GatewayProperties();
        NimbusResourceServerBinder binder = new NimbusResourceServerBinder(properties);

        assertThat(binder.getType()).isEqualTo("nimbus");
        assertThat(binder.jwtDecoder()).isNotNull();
    }

    @Test
    @DisplayName("rejects a shared secret shorter than 32 bytes")
    void rejectsShortSecret() {
        GatewayProperties properties = new GatewayProperties();
        properties.getSecurity().setSecret("too-short");

        assertThatThrownBy(() -> new NimbusResourceServerBinder(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    @DisplayName("prefers the JWK set URI over the shared secret")
    void prefersJwkSetUri() {
        GatewayProperties properties = new GatewayProperties();
        properties.getSecurity().setJwkSetUri("https://sso.example.com/certs");
        properties.getSecurity().setSecret("this-secret-is-ignored-when-jwk-set-is-set-123");

        NimbusResourceServerBinder binder = new NimbusResourceServerBinder(properties);
        assertThat(binder.jwtDecoder()).isNotNull();
    }
}
