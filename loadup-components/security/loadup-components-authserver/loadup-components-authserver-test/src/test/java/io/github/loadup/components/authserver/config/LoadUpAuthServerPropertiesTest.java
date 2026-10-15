/*
 * #%L
 * LoadUp Components AuthServer Test
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
package io.github.loadup.components.authserver.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("LoadUpAuthServerProperties")
class LoadUpAuthServerPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(PropertiesConfiguration.class);

    @Test
    @DisplayName("binds issuer and audience")
    void bindsIssuerAndAudience() {
        contextRunner
                .withPropertyValues(
                        "loadup.security.auth-server.issuer=https://sso.example.com/realms/loadup",
                        "loadup.security.auth-server.audience=loadup-api",
                        "loadup.security.auth-server.protocol-endpoints-enabled=false",
                        "loadup.security.auth-server.user-access-token-ttl=12m",
                        "loadup.security.auth-server.clients[0].client-id=portal",
                        "loadup.security.auth-server.clients[0].client-secret=synthetic-secret",
                        "loadup.security.auth-server.clients[0].redirect-uris[0]=https://example.test/callback",
                        "loadup.security.auth-server.clients[0].scopes[0]=openid")
                .run(context -> {
                    LoadUpAuthServerProperties properties = context.getBean(LoadUpAuthServerProperties.class);
                    assertThat(properties.getIssuer()).isEqualTo("https://sso.example.com/realms/loadup");
                    assertThat(properties.getAudience()).isEqualTo("loadup-api");
                    assertThat(properties.isProtocolEndpointsEnabled()).isFalse();
                    assertThat(properties.getUserAccessTokenTtl()).isEqualTo(java.time.Duration.ofMinutes(12));
                    assertThat(properties.getClients().getFirst().getRedirectUris())
                            .containsExactly("https://example.test/callback");
                    assertThat(properties.getClients().getFirst().getScopes()).containsExactly("openid");
                });
    }

    @Test
    @DisplayName("sas binder defaults are applied")
    void bindsSasDefaults() {
        contextRunner.run(context -> {
            LoadUpAuthServerProperties properties = context.getBean(LoadUpAuthServerProperties.class);
            assertThat(properties.getIssuer()).isEqualTo("http://localhost:8080");
            assertThat(properties.getJwk().getKid()).isEqualTo("loadup");
        });
    }

    @EnableConfigurationProperties(LoadUpAuthServerProperties.class)
    static class PropertiesConfiguration {}
}
