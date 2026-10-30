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
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.time.Duration;
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

    private static KmsProperties settings(
            URI endpoint,
            String mount,
            String namespace,
            String tokenEnv,
            String managementTokenEnv,
            Integer maxInput) {
        return new KmsProperties(
                endpoint, mount, namespace, tokenEnv, managementTokenEnv, false, null, null, null, maxInput);
    }

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
    void consumerRuntimeAndManagerBeansTakePrecedence() {
        KmsTemplate runtime = mock(KmsTemplate.class);
        KmsKeyManager manager = mock(KmsKeyManager.class);
        context.withPropertyValues(
                        "loadup.kms.enabled=true",
                        "loadup.kms.management-enabled=true",
                        "loadup.kms.endpoint=https://bao.example.test")
                .withBean(KmsTemplate.class, () -> runtime)
                .withBean(KmsKeyManager.class, () -> manager)
                .run(application -> {
                    assertThat(application)
                            .hasNotFailed()
                            .hasSingleBean(KmsTemplate.class)
                            .hasSingleBean(KmsKeyManager.class);
                    assertThat(application.getBean(KmsTemplate.class)).isSameAs(runtime);
                    assertThat(application.getBean(KmsKeyManager.class)).isSameAs(manager);
                });
    }

    @Test
    void explicitDisableDoesNotAssembleManagementEvenWhenRequested() {
        context.withPropertyValues("loadup.kms.enabled=false", "loadup.kms.management-enabled=true")
                .run(application -> assertThat(application)
                        .hasNotFailed()
                        .doesNotHaveBean(KmsTemplate.class)
                        .doesNotHaveBean(KmsKeyManager.class));
    }

    @Test
    void environmentCredentialsFallbackCanBeAssembledWithoutReadingSecretsOrConnecting() {
        context.withPropertyValues(
                        "loadup.kms.enabled=true",
                        "loadup.kms.management-enabled=true",
                        "loadup.kms.endpoint=https://bao.example.test",
                        "loadup.kms.token-env=LOADUP_TEST_RUNTIME_TOKEN",
                        "loadup.kms.management-token-env=LOADUP_TEST_ADMIN_TOKEN")
                .run(application -> assertThat(application)
                        .hasNotFailed()
                        .hasSingleBean(KmsTemplate.class)
                        .hasSingleBean(KmsKeyManager.class));
    }

    @Test
    void constructorRejectsInvalidMountCredentialNamesNamespaceAndSize() {
        URI endpoint = URI.create("https://bao.example.test");
        for (String mount : new String[] {"", "../transit", "/transit", "transit/", "transit//keys", "m".repeat(129)}) {
            assertThatThrownBy(() -> settings(endpoint, mount, null, null, null, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        for (String name : new String[] {"", "1TOKEN", "TOKEN-NAME"}) {
            assertThatThrownBy(() -> settings(endpoint, "transit", null, name, null, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> settings(endpoint, "transit", null, null, name, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        for (String namespace : new String[] {"", " ", "tenant\r", "tenant\n"}) {
            assertThatThrownBy(() -> settings(endpoint, "transit", namespace, null, null, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        for (int size : new int[] {0, -1, 1024 * 1024 + 1}) {
            assertThatThrownBy(() -> settings(endpoint, "transit", null, null, null, size))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> settings(null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> settings(URI.create("https:/relative"), null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> settings(URI.create("https://bao.example#fragment"), null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validExplicitPropertiesBindThroughTheRuntimeConfiguration() {
        context.withPropertyValues(
                        "loadup.kms.enabled=true",
                        "loadup.kms.endpoint=https://bao.example.test",
                        "loadup.kms.mount=payments/transit",
                        "loadup.kms.namespace=merchant",
                        "loadup.kms.max-input-bytes=1048576",
                        "loadup.kms.connect-timeout=1ms",
                        "loadup.kms.read-timeout=24h")
                .withBean(KmsTokenProvider.class, () -> () -> "runtime")
                .run(application -> {
                    assertThat(application).hasNotFailed().hasSingleBean(KmsTemplate.class);
                    KmsProperties properties = application.getBean(KmsProperties.class);
                    assertThat(properties.mount()).isEqualTo("payments/transit");
                    assertThat(properties.namespace()).isEqualTo("merchant");
                    assertThat(properties.maxInputBytes()).isEqualTo(1024 * 1024);
                    assertThat(properties.connectTimeout()).isEqualTo(Duration.ofMillis(1));
                    assertThat(properties.readTimeout()).isEqualTo(Duration.ofDays(1));
                });
    }

    @Test
    void invalidTransportTimeoutFailsRuntimeAssembly() {
        context.withPropertyValues(
                        "loadup.kms.enabled=true",
                        "loadup.kms.endpoint=https://bao.example.test",
                        "loadup.kms.read-timeout=0ms")
                .run(application -> assertThat(application).hasFailed());
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
