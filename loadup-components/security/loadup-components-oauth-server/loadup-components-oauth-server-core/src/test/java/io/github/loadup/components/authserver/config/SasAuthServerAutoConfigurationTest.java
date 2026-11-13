/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.authserver.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@DisplayName("SasAuthServerAutoConfiguration")
class SasAuthServerAutoConfigurationTest {

    @TempDir
    Path directory;

    @Test
    @DisplayName("does not create or overwrite client registrations")
    void registersStandardBeans() throws Exception {
        var file = TestKeyStores.create(directory, "auth-current");
        TestKeyStores.runner(file)
                .withPropertyValues(
                        "loadup.security.auth-server.enabled=true",
                        "loadup.security.auth-server.protocol-endpoints-enabled=true",
                        "loadup.security.auth-server.issuer=http://localhost:8080",
                        "loadup.security.auth-server.audience=loadup-api")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RegisteredClientRepository.class);
                    assertThat(context).hasSingleBean(AuthorizationServerSettings.class);
                    assertThat(context).hasSingleBean(OAuth2TokenCustomizer.class);
                    assertThat(context).hasNotFailed();

                    assertThat(context.getBean(AuthorizationServerSettings.class)
                                    .getIssuer())
                            .isEqualTo("http://localhost:8080");
                });
    }
}
