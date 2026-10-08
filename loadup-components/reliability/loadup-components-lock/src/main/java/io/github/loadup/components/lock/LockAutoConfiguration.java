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

import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reuses an application-owned client or creates a client from explicitly supplied native configuration. */
@AutoConfiguration(
        afterName = {
            "org.redisson.spring.starter.RedissonAutoConfigurationV2",
            "org.redisson.spring.starter.RedissonAutoConfiguration"
        })
@ConditionalOnProperty(prefix = "loadup.lock", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(LockProperties.class)
public class LockAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(LockTemplate.class)
    public LockTemplate lockTemplate(
            RedissonClient client, LockProperties properties, ObjectProvider<MeterRegistry> metrics) {
        return new RedissonLockTemplate(client, properties.namespace(), properties.options(), metrics.getIfAvailable());
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(RedissonClient.class)
    @ConditionalOnProperty(prefix = "loadup.lock", name = "redisson-config")
    static class ClientConfiguration {
        @Bean(destroyMethod = "shutdown")
        RedissonClient loadUpLockRedissonClient(LockProperties properties) throws IOException {
            LockKey.required(properties.namespace(), "namespace", 128);
            properties.options();
            if (properties.redissonConfig() == null)
                throw new IllegalArgumentException("Provide a RedissonClient bean or loadup.lock.redisson-config");
            try (var input = properties.redissonConfig().getInputStream()) {
                return Redisson.create(Config.fromYAML(input));
            }
        }
    }
}
