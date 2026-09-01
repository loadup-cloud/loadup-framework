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

import io.github.loadup.components.authserver.sas.SasAuthServerAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@DisplayName("SasAuthServerAutoConfiguration")
class SasAuthServerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SasAuthServerAutoConfiguration.class));

    @Test
    @DisplayName("registers the standard authorization server beans from yml clients")
    void registersStandardBeans() {
        contextRunner
                .withPropertyValues(
                        "loadup.components.authserver.issuer=http://localhost:8080",
                        "loadup.components.authserver.clients[0].client-id=loadup-app",
                        "loadup.components.authserver.clients[0].client-secret=change-me",
                        "loadup.components.authserver.clients[0].scopes[0]=openid",
                        "loadup.components.authserver.clients[0].grant-types[0]=client_credentials",
                        "loadup.components.authserver.clients[0].grant-types[1]=refresh_token")
                .run(context -> {
                    assertThat(context).hasSingleBean(RegisteredClientRepository.class);
                    assertThat(context).hasSingleBean(AuthorizationServerSettings.class);
                    assertThat(context).hasSingleBean(OAuth2TokenCustomizer.class);
                    assertThat(context).hasNotFailed();

                    RegisteredClientRepository repository = context.getBean(RegisteredClientRepository.class);
                    assertThat(repository.findByClientId("loadup-app")).isNotNull();
                    assertThat(repository.findByClientId("missing")).isNull();
                    assertThat(context.getBean(AuthorizationServerSettings.class)
                                    .getIssuer())
                            .isEqualTo("http://localhost:8080");
                });
    }
}
