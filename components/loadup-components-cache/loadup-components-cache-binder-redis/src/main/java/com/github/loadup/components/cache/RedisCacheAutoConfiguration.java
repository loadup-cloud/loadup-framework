package com.github.loadup.components.cache;

/*-
 * #%L
 * loadup-components-cache-binder-redis
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.commons.util.date.DurationUtils;
import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.cfg.LoadUpRedisCacheProperties;
import com.github.loadup.components.cache.impl.RedisCacheBinderImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class RedisCacheAutoConfiguration {

    private final LoadUpRedisCacheProperties loadUpRedisCacheProperties;

    public RedisCacheAutoConfiguration(LoadUpRedisCacheProperties loadUpRedisCacheProperties) {
        this.loadUpRedisCacheProperties = loadUpRedisCacheProperties;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    @Qualifier("redisCacheManager")
    public LoadUpRedisCacheManager redisCacheManager(
            RedisConnectionFactory redisConnectionFactory, CacheProperties cacheProperties) {
        // 默认缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        // 解析 application.yml 中的 cache-config
        loadUpRedisCacheProperties
                .getCacheConfig()
                .forEach((cacheName, timeout) -> cacheConfigurations.put(
                        cacheName,
                        redisCacheConfiguration(cacheProperties)
                                .entryTtl(DurationUtils.parse(timeout.getExpireAfterWrite()))));

        LoadUpRedisCacheManager loadUpRedisCacheManager = new LoadUpRedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
                redisCacheConfiguration(cacheProperties),cacheConfigurations);
        return loadUpRedisCacheManager;
    }

    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = JsonUtil.initObjectMapper();
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        // 获取Properties中Redis的配置信息
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        // 获取RedisCacheConfiguration的默认配置对象
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // 指定序列化器为GenericJackson2JsonRedisSerializer

        config = config.serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        config = config.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer()));
        // 过期时间设置
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        // 缓存空值配置
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        // 是否启用前缀
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        config = config.computePrefixWith(name -> name + ":"); // 覆盖默认key双冒号  CacheKeyPrefix#prefixed
        return config;
    }

    @Bean(name = "redisCacheBinder")
    @ConditionalOnMissingBean(CacheBinder.class)
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheBinder cacheBinder() {
        return new RedisCacheBinderImpl();
    }
}
