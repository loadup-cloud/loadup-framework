/*
 * #%L
 * LoadUp Components Signature
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
package io.github.loadup.components.signature.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.kms.KmsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

class RequestSignatureAutoConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RequestSignatureAutoConfiguration.class))
            .withBean(KmsTemplate.class, () -> mock(KmsTemplate.class));

    @Test
    void isOptIn() {
        context.run(application ->
                assertThat(application).doesNotHaveBean(RequestSigner.class).doesNotHaveBean(RequestVerifier.class));
    }

    @Test
    void requiresReplayProtectionWhenVerificationIsEnabled() {
        context.withPropertyValues("loadup.components.signature.request.enabled=true")
                .run(application -> assertThat(application).hasFailed());
    }

    @Test
    void assemblesUsingExistingRedisTemplate() {
        context.withPropertyValues("loadup.components.signature.request.enabled=true")
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .run(application -> assertThat(application)
                        .hasSingleBean(RequestSigner.class)
                        .hasSingleBean(RequestVerifier.class)
                        .hasSingleBean(RedisRequestNonceStore.class));
    }

    @Test
    void customAtomicStoreOverridesRedisAdapter() {
        context.withPropertyValues("loadup.components.signature.request.enabled=true")
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .withBean(RequestNonceStore.class, () -> (key, ttl) -> true)
                .run(application -> assertThat(application)
                        .hasSingleBean(RequestVerifier.class)
                        .doesNotHaveBean(RedisRequestNonceStore.class));
    }

    @Test
    void outboundOnlyWorksWithoutRedisOnClasspath() {
        context.withPropertyValues(
                        "loadup.components.signature.request.enabled=true",
                        "loadup.components.signature.request.verification-enabled=false")
                .withClassLoader(new FilteredClassLoader("org.springframework.data.redis"))
                .run(application -> assertThat(application)
                        .hasSingleBean(RequestSigner.class)
                        .doesNotHaveBean(RequestVerifier.class));
    }
}
