package io.github.loadup.components.cache.jetcache.autoconfig;

/*-
 * #%L
 * Loadup Cache Binder JetCache
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

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.redis.springdata.RedisSpringDataCacheBuilder;
import com.alicp.jetcache.template.CacheBuilderTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.loadup.components.cache.CacheBackendType;
import io.github.loadup.components.cache.LoadupCacheProperties;
import io.github.loadup.components.cache.autoconfig.LoadupCacheAutoConfiguration;
import io.github.loadup.components.cache.codec.CacheJsonCodec;
import io.github.loadup.components.cache.jetcache.JetCacheCacheBinderProperties;
import io.github.loadup.components.cache.jetcache.JetCacheSpringCacheManager;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * JetCache binder: adapts the Spring Cache facade to JetCache when
 * {@code loadup.cache.type=jetcache}. JetCache is wired programmatically (local Caffeine level plus
 * an optional Spring Data Redis remote level); the remote level appears only when a
 * {@link RedisConnectionFactory} exists, so LOCAL-only usage needs no Redis at all.
 */
@AutoConfiguration(after = LoadupCacheAutoConfiguration.class, before = CacheAutoConfiguration.class)
@ConditionalOnClass({SimpleCacheManager.class, CacheBuilderTemplate.class})
@ConditionalOnProperty(prefix = "loadup.cache", name = "type", havingValue = CacheBackendType.JETCACHE)
@EnableConfigurationProperties(JetCacheCacheBinderProperties.class)
public class JetCacheCacheAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(com.alicp.jetcache.CacheManager.class)
    public com.alicp.jetcache.CacheManager jetCacheBackend(
            JetCacheCacheBinderProperties binderProperties,
            ObjectProvider<RedisConnectionFactory> connectionFactoryProvider) {
        Map<String, CacheBuilder> localBuilders = new HashMap<>();
        localBuilders.put(
                "default", CaffeineCacheBuilder.createCaffeineCacheBuilder().limit(binderProperties.getLocalLimit()));

        Map<String, CacheBuilder> remoteBuilders = new HashMap<>();
        RedisConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
        if (connectionFactory != null) {
            remoteBuilders.put(
                    "default",
                    RedisSpringDataCacheBuilder.createBuilder()
                            .connectionFactory(connectionFactory)
                            .keyPrefixSupplier(() -> binderProperties.getRemoteKeyPrefix()));
        }

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCacheBuilderTemplate(
                new CacheBuilderTemplate(binderProperties.isPenetrationProtect(), localBuilders, remoteBuilders));
        return manager;
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(
            com.alicp.jetcache.CacheManager jetCacheBackend,
            LoadupCacheProperties properties,
            JetCacheCacheBinderProperties binderProperties,
            ObjectProvider<RedisConnectionFactory> connectionFactoryProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        ObjectMapper objectMapper =
                objectMapperProvider.getIfAvailable(() -> new ObjectMapper().registerModule(new JavaTimeModule()));
        CacheJsonCodec codec = new CacheJsonCodec(objectMapper);

        JetCacheSpringCacheManager manager = new JetCacheSpringCacheManager(
                jetCacheBackend,
                properties,
                binderProperties,
                key -> "::" + String.valueOf(key),
                codec::serialize,
                codec::deserialize,
                connectionFactoryProvider.getIfAvailable(),
                binderProperties.getRemoteKeyPrefix());
        manager.preloadConfiguredCaches();
        return new CacheManagerCustomizers(customizers.orderedStream().toList()).customize(manager);
    }
}
