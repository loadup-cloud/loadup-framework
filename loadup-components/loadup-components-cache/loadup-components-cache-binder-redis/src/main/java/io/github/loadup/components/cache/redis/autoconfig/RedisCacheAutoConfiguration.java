package io.github.loadup.components.cache.redis.autoconfig;

/*-
 * #%L
 * Loadup Cache Binder Redis
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.loadup.components.cache.CacheBackendType;
import io.github.loadup.components.cache.CacheNameSettings;
import io.github.loadup.components.cache.LoadupCacheProperties;
import io.github.loadup.components.cache.autoconfig.LoadupCacheAutoConfiguration;
import io.github.loadup.components.cache.codec.CacheJsonCodec;
import io.github.loadup.components.cache.redis.RandomTtlFunction;
import io.github.loadup.components.cache.redis.RedisCacheBinderProperties;
import java.util.LinkedHashMap;
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
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis binder: provides the Spring {@link CacheManager} backed by Spring Data Redis when
 * {@code loadup.cache.type=redis}. Reuses the standard {@code spring.data.redis.*} connection
 * settings; per-cache TTL / null caching / random expiration come from {@link LoadupCacheProperties}.
 */
@AutoConfiguration(after = LoadupCacheAutoConfiguration.class, before = CacheAutoConfiguration.class)
@ConditionalOnClass({RedisCacheManager.class, RedisConnectionFactory.class})
@ConditionalOnProperty(prefix = "loadup.cache", name = "type", havingValue = CacheBackendType.REDIS)
@EnableConfigurationProperties(RedisCacheBinderProperties.class)
public class RedisCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            LoadupCacheProperties properties,
            RedisCacheBinderProperties binderProperties,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        ObjectMapper objectMapper =
                objectMapperProvider.getIfAvailable(() -> new ObjectMapper().registerModule(new JavaTimeModule()));
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(new CacheJsonCodec(objectMapper).objectMapper());

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .prefixCacheNameWith(binderProperties.getKeyPrefix())
                .entryTtl(new RandomTtlFunction(properties.getDefaultTtl(), null));

        RedisCacheWriter cacheWriter =
                RedisCacheWriter.create(connectionFactory, configurer -> configurer.immediateWrites(true));
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
                .cacheWriter(cacheWriter)
                .cacheDefaults(defaults);
        Map<String, RedisCacheConfiguration> perCacheConfigs = new LinkedHashMap<>();
        for (Map.Entry<String, CacheNameSettings> entry : properties.getCaches().entrySet()) {
            perCacheConfigs.put(entry.getKey(), perCacheConfiguration(defaults, entry.getValue()));
        }
        if (!perCacheConfigs.isEmpty()) {
            builder.withInitialCacheConfigurations(perCacheConfigs);
        }
        RedisCacheManager manager = builder.build();
        return new CacheManagerCustomizers(customizers.orderedStream().toList()).customize(manager);
    }

    private static RedisCacheConfiguration perCacheConfiguration(
            RedisCacheConfiguration defaults, CacheNameSettings settings) {
        RedisCacheConfiguration configuration = defaults;
        if (settings.ttl() != null || settings.randomExpirationRange() != null) {
            configuration =
                    configuration.entryTtl(new RandomTtlFunction(settings.ttl(), settings.randomExpirationRange()));
        }
        if (settings.allowNullValues() != null) {
            configuration = settings.allowNullValues() ? configuration : configuration.disableCachingNullValues();
        }
        return configuration;
    }
}
