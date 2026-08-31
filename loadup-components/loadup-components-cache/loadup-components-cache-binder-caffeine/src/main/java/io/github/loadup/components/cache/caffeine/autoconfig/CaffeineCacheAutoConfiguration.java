package io.github.loadup.components.cache.caffeine.autoconfig;

/*-
 * #%L
 * Loadup Cache Binder Caffeine
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.components.cache.CacheBackendType;
import io.github.loadup.components.cache.LoadupCacheProperties;
import io.github.loadup.components.cache.autoconfig.LoadupCacheAutoConfiguration;
import io.github.loadup.components.cache.caffeine.CaffeineCacheProperties;
import io.github.loadup.components.cache.caffeine.LoadupCaffeineCacheManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizers;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;

/**
 * Caffeine binder: provides the Spring {@link CacheManager} when {@code loadup.cache.type=caffeine}
 * (the default). Business code only sees the Spring Cache facade.
 */
@AutoConfiguration(after = LoadupCacheAutoConfiguration.class, before = CacheAutoConfiguration.class)
@ConditionalOnClass(Caffeine.class)
@ConditionalOnProperty(
        prefix = "loadup.cache",
        name = "type",
        havingValue = CacheBackendType.CAFFEINE,
        matchIfMissing = true)
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class CaffeineCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(
            LoadupCacheProperties properties,
            CaffeineCacheProperties binderProperties,
            ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        LoadupCaffeineCacheManager manager =
                new LoadupCaffeineCacheManager(properties, binderProperties.getMaximumSize());
        return new CacheManagerCustomizers(customizers.orderedStream().toList()).customize(manager);
    }
}
