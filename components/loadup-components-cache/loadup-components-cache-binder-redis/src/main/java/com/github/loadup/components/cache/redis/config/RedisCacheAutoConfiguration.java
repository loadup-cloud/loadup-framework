package com.github.loadup.components.cache.redis.config;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.cfg.CacheConfigs;
import com.github.loadup.components.cache.redis.binder.RedisCacheBinder;
import com.github.loadup.components.cache.redis.cfg.RedisBinderCfg;
import com.github.loadup.components.cache.util.CacheExpirationUtil;
import com.github.loadup.framework.api.enums.BinderEnum;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties({CacheBindingCfg.class, RedisBinderCfg.class})
@ConditionalOnProperty(prefix = "loadup.cache", name = "binder", havingValue = "redis")
public class RedisCacheAutoConfiguration {

  @Resource private CacheBindingCfg cacheBindingCfg;

  @Bean
  @Qualifier("redisCacheManager")
  @ConditionalOnProperty(prefix = "loadup.cache", name = "binder", havingValue = "redis")
  public LoadUpRedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
    // 默认缓存配置
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

    // Support new unified configuration structure
    Map<String, CacheConfigs> cacheConfigs = getCacheConfigs();

    if (cacheConfigs != null && !cacheConfigs.isEmpty()) {
      cacheConfigs.forEach(
          (cacheName, config) -> {
            // Only configure Redis caches (check binder selection)
            if (cacheBindingCfg.getBinderForCache(cacheName) == BinderEnum.CacheBinder.REDIS) {
              RedisCacheConfiguration redisConfig = buildCacheConfiguration(config);
              cacheConfigurations.put(cacheName, redisConfig);

              log.info("Configured Redis cache: name={}, config={}", cacheName, config);
            }
          });
    }

    return new LoadUpRedisCacheManager(
        RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
        redisCacheConfiguration(),
        cacheConfigurations);
  }

  /**
   * Get cache configurations supporting both new and legacy structure Priority: new cacheConfigs
   * map > legacy redis.cacheConfig
   */
  private Map<String, CacheConfigs> getCacheConfigs() {
    // Try new structure first
    if (cacheBindingCfg.getCacheConfigs() != null && !cacheBindingCfg.getCacheConfigs().isEmpty()) {
      return cacheBindingCfg.getCacheConfigs();
    }
    return new HashMap<>();
  }

  /** Build cache configuration with anti-avalanche strategies */
  private RedisCacheConfiguration buildCacheConfiguration(CacheConfigs cacheConfig) {
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
    ObjectMapper objectMapper = JsonUtil.getObjectMapper();
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
  @ConditionalOnProperty(prefix = "loadup.cache", name = "binder", havingValue = "redis")
  public CacheBinder cacheBinder() {
    return new RedisCacheBinder();
  }
}
