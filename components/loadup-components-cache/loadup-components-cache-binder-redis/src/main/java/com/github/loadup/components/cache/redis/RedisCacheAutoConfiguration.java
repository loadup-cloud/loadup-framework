package com.github.loadup.components.cache.redis;

/*-
 * #%L
 * loadup-components-cache-binder-redis
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.*;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.cfg.LoadUpCacheConfig;
import com.github.loadup.components.cache.config.CacheProperties;
import com.github.loadup.components.cache.redis.impl.RedisCacheBinderImpl;
import com.github.loadup.components.cache.util.CacheExpirationUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(prefix = "loadup.cache", name = "type", havingValue = "redis")
public class RedisCacheAutoConfiguration {

  @Resource private CacheProperties cacheProperties;

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    CacheProperties.RedisConfig redisConfig = cacheProperties.getRedis();

    RedisStandaloneConfiguration redisStandaloneConfiguration =
        new RedisStandaloneConfiguration(redisConfig.getHost(), redisConfig.getPort());

    if (StringUtils.hasText(redisConfig.getPassword())) {
      redisStandaloneConfiguration.setPassword(redisConfig.getPassword());
    }

    redisStandaloneConfiguration.setDatabase(redisConfig.getDatabase());

    return new LettuceConnectionFactory(redisStandaloneConfiguration);
  }

  @Bean
  @Qualifier("redisCacheManager")
  public LoadUpRedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
    // 默认缓存配置
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

    // 为每个 cache name 配置不同的策略
    cacheProperties
        .getRedis()
        .getCacheConfig()
        .forEach(
            (cacheName, cacheConfig) -> {
              RedisCacheConfiguration config = buildCacheConfiguration(cacheConfig);
              cacheConfigurations.put(cacheName, config);

              log.info("Configured Redis cache: name={}, config={}", cacheName, cacheConfig);
            });

    return new LoadUpRedisCacheManager(
        RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
        redisCacheConfiguration(),
        cacheConfigurations);
  }

  /** Build cache configuration with anti-avalanche strategies */
  private RedisCacheConfiguration buildCacheConfiguration(LoadUpCacheConfig cacheConfig) {
    RedisCacheConfiguration config = redisCacheConfiguration();

    // 1. Configure base expiration time
    if (cacheConfig.getExpireAfterWrite() != null) {
      Duration baseDuration = CacheExpirationUtil.parseDuration(cacheConfig.getExpireAfterWrite());

      // 2. Apply random offset to prevent cache avalanche
      Duration finalDuration =
          CacheExpirationUtil.calculateExpirationWithRandomOffset(baseDuration, cacheConfig);

      config = config.entryTtl(finalDuration);

      log.debug(
          "Cache TTL: base={}s, final={}s (with random offset)",
          baseDuration.getSeconds(),
          finalDuration.getSeconds());
    }

    // 3. Configure null value caching to prevent cache penetration
    if (!cacheConfig.isCacheNullValues()) {
      config = config.disableCachingNullValues();
    }

    return config;
  }

  @Bean
  public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
    ObjectMapper objectMapper = JsonUtil.initObjectMapper();
    return new GenericJackson2JsonRedisSerializer(objectMapper);
  }

  @Bean
  public RedisCacheConfiguration redisCacheConfiguration() {
    // 获取RedisCacheConfiguration的默认配置对象
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

    // 指定序列化器为GenericJackson2JsonRedisSerializer
    config =
        config.serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
    config =
        config.serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                genericJackson2JsonRedisSerializer()));

    // 默认过期时间设置为1小时
    config = config.entryTtl(Duration.ofHours(1));

    // 不缓存空值
    config = config.disableCachingNullValues();

    // 覆盖默认key双冒号
    config = config.computePrefixWith(name -> name + ":");

    return config;
  }

  @Bean(name = "redisCacheBinder")
  public CacheBinder cacheBinder() {
    return new RedisCacheBinderImpl();
  }
}
