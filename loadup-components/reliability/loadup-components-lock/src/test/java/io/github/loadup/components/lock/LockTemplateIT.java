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

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.cache.SharedRedisContainer;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = LockTemplateIT.Application.class,
        properties = {"loadup.lock.enabled=true", "loadup.lock.namespace=lock-it"})
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.REDIS)
class LockTemplateIT {
    private final LockTemplate first;

    @Autowired
    LockTemplateIT(LockTemplate first) {
        this.first = first;
    }

    @Test
    void independentClientContendsWithVirtualThreadAndCanAcquireAfterRelease() throws Exception {
        RedissonClient client = newClient();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            LockTemplate second =
                    new RedissonLockTemplate(client, "lock-it", LockOptions.watchdog(Duration.ZERO), null);
            var key = LockKey.global("mutual-exclusion", UUID.randomUUID().toString());
            var acquired = new CountDownLatch(1);
            var finish = new CountDownLatch(1);
            var holder = executor.submit(() -> first.execute(key, () -> {
                assertThat(Thread.currentThread().isVirtual()).isTrue();
                acquired.countDown();
                if (!finish.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("test release timeout");
                return "done";
            }));
            try {
                assertThat(acquired.await(10, TimeUnit.SECONDS)).isTrue();
                assertThatThrownBy(() -> second.run(key, () -> fail("must not execute")))
                        .isInstanceOf(LockUnavailableException.class);
                second.run(LockKey.tenant(key.business(), "tenant", key.resourceId()), () -> {});
            } finally {
                finish.countDown();
            }
            assertThat(holder.get(10, TimeUnit.SECONDS)).isEqualTo("done");
            second.run(key, () -> {});
        } finally {
            client.shutdown();
        }
    }

    @Test
    void watchdogRenewsBeyondInitialLease() throws Exception {
        RedissonClient client = newClient();
        try {
            var second = new RedissonLockTemplate(client, "lock-it", LockOptions.watchdog(Duration.ZERO), null);
            var key = LockKey.global("renewal", UUID.randomUUID().toString());
            first.execute(key, () -> {
                first.run(key, () -> {});
                Thread.sleep(3500);
                assertThatThrownBy(() -> second.run(key, () -> fail("must not execute")))
                        .isInstanceOf(LockUnavailableException.class);
                return null;
            });
            second.run(key, () -> {});
        } finally {
            client.shutdown();
        }
    }

    private static RedissonClient newClient() {
        var config = new Config();
        config.setLockWatchdogTimeout(2000);
        config.useSingleServer().setAddress(SharedRedisContainer.getUrl());
        return Redisson.create(config);
    }

    @SpringBootConfiguration
    @Import(LockAutoConfiguration.class)
    static class Application {
        @Bean(destroyMethod = "shutdown")
        RedissonClient redissonClient() {
            return newClient();
        }
    }
}
