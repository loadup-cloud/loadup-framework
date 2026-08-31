package io.github.loadup.components.cache.redis.autoconfig;

/*-
 * #%L
 * Loadup Cache Binder Redis
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
