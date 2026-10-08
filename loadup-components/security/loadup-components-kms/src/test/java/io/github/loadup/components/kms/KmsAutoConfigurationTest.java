/*
 * #%L
 * LoadUp Kms
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
package io.github.loadup.components.kms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class KmsAutoConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KmsAutoConfiguration.class))
            .withBean(RestClient.Builder.class, RestClient::builder)
            .withBean(ObjectMapper.class, () -> JsonMapper.builder().build());

    @Test
    void remainsDisabledWithoutExplicitConfiguration() {
        context.run(application -> {
            assertThat(application).doesNotHaveBean(KmsTemplate.class);
            assertThat(application).doesNotHaveBean(KmsKeyManager.class);
        });
    }

    @Test
    void assemblesRuntimeWithoutEnablingAdministrationOrMakingNetworkCalls() {
        context.withPropertyValues("loadup.kms.enabled=true", "loadup.kms.endpoint=https://bao.example.test/")
                .withBean(KmsTokenProvider.class, () -> () -> "runtime")
                .run(application -> {
                    assertThat(application).hasSingleBean(KmsTemplate.class).doesNotHaveBean(KmsKeyManager.class);
                    assertThat(application.getBean(KmsProperties.class).endpoint())
                            .isEqualTo(URI.create("https://bao.example.test"));
                });
    }

    @Test
    void assemblesManagementUsingItsOwnProvider() {
        context.withPropertyValues(
                        "loadup.kms.enabled=true",
                        "loadup.kms.management-enabled=true",
                        "loadup.kms.endpoint=https://bao.example.test")
                .withBean(KmsTokenProvider.class, () -> () -> "runtime")
                .withBean(KmsManagementTokenProvider.class, () -> () -> "management")
                .run(application -> {
                    assertThat(application).hasSingleBean(KmsTemplate.class).hasSingleBean(KmsKeyManager.class);
                });
    }

    @Test
    void rejectsInsecureEndpointAndUntrustedPaths() {
        for (String endpoint : new String[] {
            "http://bao.test", "https://user:password@bao.test", "https://bao.test/v1", "https://bao.test?token=secret"
        }) {
            assertThatThrownBy(() -> new KmsProperties(
                            URI.create(endpoint), null, null, null, null, false, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> KmsKeyRef.latest("../admin")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new KmsCiphertext("merchant", 2, "vault:v1:AA=="))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
