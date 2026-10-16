/*
 * #%L
 * LoadUp Async Result Test
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
package io.github.loadup.components.asyncresult;

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.components.asyncresult.local.LocalAsyncResultAutoConfiguration;
import io.github.loadup.components.asyncresult.redis.RedisAsyncResultAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AsyncResultAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
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
    void missingNamespaceAndInvalidBinderOrLimitsFailStartup() {
        runner.withPropertyValues("loadup.components.async-result.enabled=true")
                .run(c -> assertThat(c).hasFailed());
        for (String setting : new String[] {
            "binder-type=unknown",
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
        })
            runner.withPropertyValues(
                            "loadup.components.async-result.enabled=true",
                            "loadup.components.async-result.namespace=test",
                            "loadup.components.async-result." + setting)
                    .run(c -> assertThat(c).hasFailed());
        runner.withPropertyValues(
                        "loadup.components.async-result.enabled=true",
                        "loadup.components.async-result.namespace=test",
                        "loadup.components.async-result.binder-type=redis")
                .run(c -> assertThat(c).hasFailed());
    }

    @Test
    void keysValuesAndOptionsValidateTheirPublicContracts() {
        assertThatThrownBy(() -> new AsyncReplyKey("t", "op", "b", "bad")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultOptions("", "op", "b", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultOptions("t", "op", "b", java.time.Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultValue(null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultValue("1", "FAIL")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultValue(null, "bad code")).isInstanceOf(IllegalArgumentException.class);
        assertThat(AsyncResultTest.options().toString()).contains("tenantId");
        assertThat(LocalAsyncResultProviderTest.key().toString()).contains("correlationId");
    }

    @Test
    void explicitDefaultsAreDocumentedAndKeysRejectNonCanonicalIdentifiers() {
        var properties = new AsyncResultProperties(false, null, null, null, null, 0, null, null, 0, 0);
        assertThat(properties.toString()).contains("maxPending");
        assertThat(properties.maxPending()).isEqualTo(10000);
        assertThatThrownBy(() -> new AsyncReplyKey("tenant", "op", "b", "ABCDEFAB-ABCD-ABCD-ABCD-ABCDEFABCDEF"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultOptions("t".repeat(65), "op", "b", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AsyncResultOptions("t", "op", "b", java.time.Duration.ofMillis(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
