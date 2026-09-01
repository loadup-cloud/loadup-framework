package io.github.loadup.components.authserver.config;

/*-
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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.BinderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("LoadUpAuthServerProperties")
class LoadUpAuthServerPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(PropertiesConfiguration.class);

    @Test
    @DisplayName("binds keycloak binder and issuer/jwk-set-uri")
    void bindsKeycloakBinder() {
        contextRunner
                .withPropertyValues(
                        "loadup.components.authserver.binder-type=keycloak",
                        "loadup.components.authserver.issuer=https://sso.example.com/realms/loadup",
                        "loadup.components.authserver.jwk-set-uri=https://sso.example.com/realms/loadup/protocol/openid-connect/certs")
                .run(context -> {
                    LoadUpAuthServerProperties properties = context.getBean(LoadUpAuthServerProperties.class);
                    assertThat(properties.getBinderType()).isEqualTo(BinderType.KEYCLOAK);
                    assertThat(properties.getIssuer()).isEqualTo("https://sso.example.com/realms/loadup");
                    assertThat(properties.getJwkSetUri())
                            .isEqualTo("https://sso.example.com/realms/loadup/protocol/openid-connect/certs");
                });
    }

    @Test
    @DisplayName("sas binder defaults are applied")
    void bindsSasDefaults() {
        contextRunner.run(context -> {
            LoadUpAuthServerProperties properties = context.getBean(LoadUpAuthServerProperties.class);
            assertThat(properties.getBinderType()).isEqualTo(BinderType.SAS);
            assertThat(properties.getIssuer()).isEqualTo("http://localhost:8080");
            assertThat(properties.getJwk().getKid()).isEqualTo("loadup");
        });
    }

    @EnableConfigurationProperties(LoadUpAuthServerProperties.class)
    static class PropertiesConfiguration {}
}
