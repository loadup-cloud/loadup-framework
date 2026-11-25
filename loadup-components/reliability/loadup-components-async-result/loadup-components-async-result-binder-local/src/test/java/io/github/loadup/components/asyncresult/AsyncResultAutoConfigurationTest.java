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
package io.github.loadup.components.asyncresult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.asyncresult.local.LocalAsyncResultAutoConfiguration;
import io.github.loadup.components.asyncresult.local.LocalAsyncResultProvider;
import io.github.loadup.components.asyncresult.redis.RedisAsyncResultAutoConfiguration;
import io.github.loadup.components.runtime.json.JsonAutoConfiguration;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

class AsyncResultAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JsonAutoConfiguration.class,
                    JacksonAutoConfiguration.class,
                    AsyncResultAutoConfiguration.class,
                    LocalAsyncResultAutoConfiguration.class,
                    RedisAsyncResultAutoConfiguration.class));

    @Test
    void disabledByDefaultAndLocalIsSelectedOnlyWhenEnabled() {
        runner.run(c -> assertThat(c).doesNotHaveBean(AsyncResultTemplate.class));
        runner.withPropertyValues(
                        "loadup.components.async-result.enabled=true", "loadup.components.async-result.namespace=test")
                .run(c -> {
                    assertThat(c)
                            .hasNotFailed()
                            .hasSingleBean(AsyncResultProvider.class)
                            .hasSingleBean(AsyncResultTemplate.class);
                    var template = c.getBean(AsyncResultTemplate.class);
                    assertThat(template.execute(AsyncResultTest.options(), String.class, k -> {
                                template.complete(k, "ok");
                                return AsyncResultTest.sent();
                            }))
                            .isEqualTo("ok");
                });
    }

    @Test
    void assemblesWithThirdPartyBinderRegisteredAfterTheApiConfiguration() {
        runner.withConfiguration(AutoConfigurations.of(TestBinderAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.components.async-result.enabled=true",
                        "loadup.components.async-result.namespace=test",
                        "loadup.components.async-result.binder-type=third-party")
                .run(c -> {
                    assertThat(c)
                            .hasSingleBean(AsyncResultProvider.class)
                            .hasBean("thirdPartyAsyncResultProvider")
                            .hasSingleBean(AsyncResultTemplate.class);
                    var template = c.getBean(AsyncResultTemplate.class);
                    assertThat(template.execute(AsyncResultTest.options(), String.class, key -> {
                                template.complete(key, "third-party result");
                                return AsyncResultTest.sent();
                            }))
                            .isEqualTo("third-party result");
                });
    }

    @Test
    void enabledWithoutMatchingProviderFailsStartup() {
        runner.withPropertyValues(
                        "loadup.components.async-result.enabled=true",
                        "loadup.components.async-result.namespace=test",
                        "loadup.components.async-result.binder-type=unknown")
                .run(c -> assertThat(c)
                        .getFailure()
                        .hasRootCauseInstanceOf(NoSuchBeanDefinitionException.class)
                        .hasStackTraceContaining(AsyncResultProvider.class.getName()));
    }

    @Test
    void enabledWithAmbiguousProvidersFailsStartup() {
        runner.withPropertyValues(
                        "loadup.components.async-result.enabled=true", "loadup.components.async-result.namespace=test")
                .withBean("firstProvider", AsyncResultProvider.class, () -> mock(AsyncResultProvider.class))
                .withBean("secondProvider", AsyncResultProvider.class, () -> mock(AsyncResultProvider.class))
                .run(c -> assertThat(c).getFailure().hasRootCauseInstanceOf(NoUniqueBeanDefinitionException.class));
    }

    @Test
    void customTemplateWorksWithoutABinder() {
        var custom = mock(AsyncResultTemplate.class);
        runner.withPropertyValues(
                        "loadup.components.async-result.enabled=true",
                        "loadup.components.async-result.namespace=test",
                        "loadup.components.async-result.binder-type=custom")
                .withBean(AsyncResultTemplate.class, () -> custom)
                .run(c -> {
                    assertThat(c).hasSingleBean(AsyncResultTemplate.class).doesNotHaveBean(AsyncResultProvider.class);
                    assertThat(c.getBean(AsyncResultTemplate.class)).isSameAs(custom);
                });
    }

    @Test
    void missingNamespaceAndInvalidLimitsFailStartup() {
        runner.withPropertyValues("loadup.components.async-result.enabled=true")
                .run(c -> assertThat(c).hasFailed());
        for (String setting : new String[] {
            "wait-timeout=0ms",
            "max-wait-timeout=2d",
            "result-retention=8d",
            "max-retained=1",
            "max-result-bytes=-1",
            "max-pending=-1",
            "max-result-bytes=1048577",
            "wait-timeout=31s",
            "result-retention=1s",
            "fallback-poll-interval=0ms"
        }) {
            runner.withPropertyValues(
                            "loadup.components.async-result.enabled=true",
                            "loadup.components.async-result.namespace=test",
                            "loadup.components.async-result." + setting)
                    .run(c -> assertThat(c).hasFailed());
        }
        runner.withPropertyValues(
                        "loadup.components.async-result.enabled=true",
                        "loadup.components.async-result.namespace=test",
                        "loadup.components.async-result.binder-type=redis")
                .run(c -> assertThat(c).hasFailed());
    }

    @Test
    void keysValuesAndOptionsValidateTheirPublicContracts() {
        assertThatThrownBy(() -> new AsyncReplyKey("op", "b", "bad")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultOptions("op", null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultOptions("op", "b", Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultValue(null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultValue("1", "FAIL")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultValue(null, "bad code")).isInstanceOf(IllegalArgumentException.class);
        assertThat(AsyncResultTest.options().toString()).contains("operation");
        assertThat(LocalAsyncResultProviderTest.key().toString()).contains("correlationId");
    }

    @Test
    void explicitDefaultsAreDocumentedAndKeysRejectNonCanonicalIdentifiers() {
        var properties = new AsyncResultProperties(false, null, null, null, null, 0, null, null, 0, 0);
        assertThat(properties.toString()).contains("maxPending");
        assertThat(properties.maxPending()).isEqualTo(10000);
        assertThatThrownBy(() -> new AsyncReplyKey("op", "b", "ABCDEFAB-ABCD-ABCD-ABCD-ABCDEFABCDEF"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultOptions("o".repeat(129), "b", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultOptions("op", "b", Duration.ofMillis(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @AutoConfiguration(after = AsyncResultAutoConfiguration.class)
    @ConditionalOnProperty(prefix = "loadup.components.async-result", name = "enabled", havingValue = "true")
    @ConditionalOnProperty(prefix = "loadup.components.async-result", name = "binder-type", havingValue = "third-party")
    static class TestBinderAutoConfiguration {
        @Bean(destroyMethod = "close")
        LocalAsyncResultProvider thirdPartyAsyncResultProvider() {
            return new LocalAsyncResultProvider(100);
        }
    }
}
