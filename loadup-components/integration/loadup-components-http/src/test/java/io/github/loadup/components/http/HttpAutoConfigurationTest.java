/*
 * #%L
 * LoadUp Http
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
package io.github.loadup.components.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class HttpAutoConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpAutoConfiguration.class))
            .withBean(RestClient.Builder.class, RestClient::builder)
            .withBean(ObjectMapper.class, () -> JsonMapper.builder().build());

    @Test
    void bindsNamedClientsAndOperationsWithDefaults() {
        context.withPropertyValues(
                        "loadup.http.clients.demo.base-url=https://example.test",
                        "loadup.http.clients.demo.operations.query.method=POST",
                        "loadup.http.clients.demo.operations.query.path=/orders/{id}")
                .run(application -> {
                    assertThat(application).hasSingleBean(HttpTemplate.class);
                    var properties = application.getBean(HttpProperties.class);
                    assertThat(properties.clients().get("demo").readTimeout())
                            .isEqualTo(java.time.Duration.ofSeconds(10));
                    assertThat(properties.clients().get("demo").operations()).containsKey("query");
                });
    }

    @Test
    void disableSwitchPreventsAssembly() {
        context.withPropertyValues("loadup.http.enabled=false")
                .run(application -> assertThat(application).doesNotHaveBean(HttpTemplate.class));
    }

    @Test
    void explicitTemplateOverridesDefaultAssembly() {
        var custom = mock(HttpTemplate.class);
        context.withBean(HttpTemplate.class, () -> custom)
                .withPropertyValues(
                        "loadup.http.clients.demo.base-url=https://example.test",
                        "loadup.http.clients.demo.credential-ref=unused-with-custom-template")
                .run(application -> {
                    assertThat(application).hasNotFailed().hasSingleBean(HttpTemplate.class);
                    assertThat(application.getBean(HttpTemplate.class)).isSameAs(custom);
                });
    }

    @Test
    void missingMapperFailsAssemblyClearly() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(HttpAutoConfiguration.class))
                .withBean(RestClient.Builder.class, RestClient::builder)
                .run(application -> assertThat(application).hasFailed());
    }

    @Test
    void usesSharedCredentialsSslBundlesAndMeterRegistry() throws Exception {
        var bundles = mock(SslBundles.class);
        var bundle = mock(SslBundle.class);
        when(bundles.getBundle("client-tls")).thenReturn(bundle);
        when(bundle.createSslContext()).thenReturn(SSLContext.getDefault());
        var registry = new SimpleMeterRegistry();
        try {
            context.withBean(HttpCredentialProvider.class, () -> reference -> Map.of("Authorization", "Bearer managed"))
                    .withBean(SslBundles.class, () -> bundles)
                    .withBean(MeterRegistry.class, () -> registry)
                    .withPropertyValues(
                            "loadup.http.clients.demo.base-url=https://example.test",
                            "loadup.http.clients.demo.ssl-bundle=client-tls",
                            "loadup.http.clients.demo.credential-ref=credential",
                            "loadup.http.clients.demo.proxy=http://127.0.0.1:8888",
                            "loadup.http.clients.demo.operations.query.method=POST",
                            "loadup.http.clients.demo.operations.query.path=/orders")
                    .run(application -> {
                        assertThat(application).hasNotFailed().hasSingleBean(HttpTemplate.class);
                        verify(bundles).getBundle("client-tls");
                        verify(bundle).createSslContext();
                    });
        } finally {
            registry.close();
        }
    }

    @Test
    void configuredCredentialsWithoutProviderFailAtStartup() {
        context.withPropertyValues(
                        "loadup.http.clients.demo.base-url=https://example.test",
                        "loadup.http.clients.demo.credential-ref=credential")
                .run(application -> assertThat(application).hasFailed());
    }
}
