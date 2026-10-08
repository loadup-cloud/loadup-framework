/*
 * #%L
 * LoadUp Lock
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
package io.github.loadup.components.lock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LockAutoConfigurationTest {
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(LockAutoConfiguration.class));

    @Test
    void disabledDoesNotCreateClientOrTemplate() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(LockTemplate.class).doesNotHaveBean(RedissonClient.class);
        });
    }

    @Test
    void reusesExistingClientAndDoesNotLoadNativeConfig() {
        var client = mock(RedissonClient.class);
        runner.withBean(RedissonClient.class, () -> client)
                .withPropertyValues(
                        "loadup.lock.enabled=true",
                        "loadup.lock.namespace=payments",
                        "loadup.lock.redisson-config=classpath:does-not-exist.yaml")
                .run(context -> {
                    assertThat(context).hasSingleBean(LockTemplate.class).hasSingleBean(RedissonClient.class);
                    assertThat(context.getBean(RedissonClient.class)).isSameAs(client);
                    verifyNoInteractions(client);
                });
    }

    @Test
    void validatesNamespaceAndLeaseBeforeBusinessCanUseTemplate() {
        runner.withBean(RedissonClient.class, () -> mock(RedissonClient.class))
                .withPropertyValues("loadup.lock.enabled=true")
                .run(context -> assertThat(context).hasFailed());
        runner.withBean(RedissonClient.class, () -> mock(RedissonClient.class))
                .withPropertyValues(
                        "loadup.lock.enabled=true", "loadup.lock.namespace=payments", "loadup.lock.lease-mode=fixed")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void customTemplateNeedsNoUnusedClient() {
        var custom = mock(LockTemplate.class);
        runner.withBean(LockTemplate.class, () -> custom)
                .withPropertyValues("loadup.lock.enabled=true")
                .run(context -> {
                    assertThat(context.getBean(LockTemplate.class)).isSameAs(custom);
                    assertThat(context).doesNotHaveBean(RedissonClient.class);
                });
    }
}
