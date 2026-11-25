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
package io.github.loadup.components.signature.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.runtime.time.ClockAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

class RequestSignatureAutoConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(ClockAutoConfiguration.class, RequestSignatureAutoConfiguration.class));

    private final ApplicationContextRunner withCustomTemplate =
            context.withBean(KmsTemplate.class, () -> mock(KmsTemplate.class));

    @Test
    void isOptIn() {
        context.run(application ->
                assertThat(application).doesNotHaveBean(RequestSigner.class).doesNotHaveBean(RequestVerifier.class));
    }

    @Test
    void requiresReplayProtectionWhenVerificationIsEnabled() {
        withCustomTemplate
                .withPropertyValues("loadup.components.signature.request.enabled=true")
                .run(application -> assertThat(application).hasFailed());
    }

    @Test
    void assemblesUsingBinderRegisteredAfterRequestSignatureAndExistingRedisTemplate() {
        context.withConfiguration(AutoConfigurations.of(TestKmsBinder.class))
                .withPropertyValues("loadup.components.signature.request.enabled=true")
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .run(application -> assertThat(application)
                        .hasSingleBean(KmsTemplate.class)
                        .hasSingleBean(RequestSigner.class)
                        .hasSingleBean(RequestVerifier.class)
                        .hasSingleBean(RedisRequestNonceStore.class));
    }

    @Test
    void binderDefersToApplicationKmsTemplate() {
        var customTemplate = mock(KmsTemplate.class);
        context.withConfiguration(AutoConfigurations.of(TestKmsBinder.class))
                .withPropertyValues(
                        "loadup.components.signature.request.enabled=true",
                        "loadup.components.signature.request.verification-enabled=false")
                .withBean(KmsTemplate.class, () -> customTemplate)
                .run(application -> {
                    assertThat(application).hasSingleBean(RequestSigner.class).hasSingleBean(KmsTemplate.class);
                    assertThat(application.getBean(KmsTemplate.class)).isSameAs(customTemplate);
                });
    }

    @Test
    void enablingRequestsWithoutKmsFailsStartup() {
        context.withPropertyValues(
                        "loadup.components.signature.request.enabled=true",
                        "loadup.components.signature.request.verification-enabled=false")
                .run(application -> assertThat(application)
                        .getFailure()
                        .hasRootCauseInstanceOf(NoSuchBeanDefinitionException.class)
                        .hasStackTraceContaining(KmsTemplate.class.getName()));
    }

    @Test
    void customAtomicStoreOverridesRedisAdapter() {
        withCustomTemplate
                .withPropertyValues("loadup.components.signature.request.enabled=true")
                .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .withBean(RequestNonceStore.class, () -> (key, ttl) -> true)
                .run(application -> assertThat(application)
                        .hasSingleBean(RequestVerifier.class)
                        .doesNotHaveBean(RedisRequestNonceStore.class));
    }

    @Test
    void outboundOnlyWorksWithoutRedisOnClasspath() {
        withCustomTemplate
                .withPropertyValues(
                        "loadup.components.signature.request.enabled=true",
                        "loadup.components.signature.request.verification-enabled=false")
                .withClassLoader(new FilteredClassLoader("org.springframework.data.redis"))
                .run(application -> assertThat(application)
                        .hasSingleBean(RequestSigner.class)
                        .doesNotHaveBean(RequestVerifier.class));
    }

    @AutoConfiguration(after = RequestSignatureAutoConfiguration.class)
    static class TestKmsBinder {
        @Bean
        @ConditionalOnMissingBean(KmsTemplate.class)
        KmsTemplate testKmsTemplate() {
            return mock(KmsTemplate.class);
        }
    }
}
