package com.github.loadup.components.cache.caffeine;

/*-
 * #%L
 * loadup-components-cache-binder-caffeine
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.caffeine.binder.CaffeineCacheBinderImpl;
import com.github.loadup.components.cache.cfg.LoadUpCacheConfig;
import com.github.loadup.components.cache.config.CacheProperties;
import com.github.loadup.components.cache.util.CacheExpirationUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnClass({Caffeine.class, CaffeineCacheManager.class})
@AutoConfiguration(before = CacheAutoConfiguration.class)
public class CaffeineCacheAutoConfiguration {

  @Resource private CacheProperties cacheProperties;

  /** default cache manager with per-cache configurations */
  @Primary
  @Bean(name = "caffeineCacheManager")
  @ConditionalOnProperty(prefix = "loadup.cache", name = "type", havingValue = "caffeine")
  public CacheManager defaultCacheManager() {
    LoadUpCaffeineCacheManager cacheManager = new LoadUpCaffeineCacheManager();
    cacheManager.setAllowNullValues(cacheProperties.getCaffeine().isAllowNullValue());

    // Configure default cache
    CacheProperties.CaffeineConfig config = cacheProperties.getCaffeine();
    Caffeine<Object, Object> defaultCaffeine = buildDefaultCaffeine(config);
    cacheManager.setCaffeine(defaultCaffeine);

    // Configure per-cache strategies
    Map<String, LoadUpCacheConfig> cacheConfigs = cacheProperties.getCaffeine().getCacheConfig();
    if (cacheConfigs != null && !cacheConfigs.isEmpty()) {
      cacheConfigs.forEach(
          (cacheName, cacheConfig) -> {
            Caffeine<Object, Object> customCaffeine = buildCustomCaffeine(cacheConfig);
            cacheManager.registerCustomCache(cacheName, customCaffeine);

            log.info(
                "Configured Caffeine cache: name={}, maxSize={}, expireAfterWrite={}",
                cacheName,
                cacheConfig.getMaximumSize(),
                cacheConfig.getExpireAfterWrite());
          });
    }

    return cacheManager;
  }

  /** Build default Caffeine with global configuration */
  private Caffeine<Object, Object> buildDefaultCaffeine(CacheProperties.CaffeineConfig config) {
    Caffeine<Object, Object> builder =
        Caffeine.newBuilder()
            .initialCapacity(config.getInitialCapacity())
            .maximumSize(config.getMaximumSize());

    if (config.getExpireAfterAccessSeconds() > 0) {
      builder.expireAfterAccess(config.getExpireAfterAccessSeconds(), TimeUnit.SECONDS);
    }

    if (config.getExpireAfterWriteSeconds() > 0) {
      // Apply random offset for default cache
      long baseSeconds = config.getExpireAfterWriteSeconds();
      Duration baseDuration = Duration.ofSeconds(baseSeconds);
      Duration finalDuration =
          CacheExpirationUtil.calculateExpirationWithPercentageOffset(
              baseDuration, 0.1); // 10% random offset

      builder.expireAfterWrite(finalDuration.getSeconds(), TimeUnit.SECONDS);

      log.debug("Default cache TTL: base={}s, final={}s", baseSeconds, finalDuration.getSeconds());
    }

    return builder;
  }

  /** Build custom Caffeine for specific cache with anti-avalanche strategies */
  private Caffeine<Object, Object> buildCustomCaffeine(LoadUpCacheConfig cacheConfig) {
    Caffeine<Object, Object> builder = Caffeine.newBuilder();

    // Set maximum size
    if (cacheConfig.getMaximumSize() > 0) {
      builder.maximumSize(cacheConfig.getMaximumSize());
    }

    // Configure expireAfterAccess with random offset
    if (cacheConfig.getExpireAfterAccess() != null) {
      Duration baseDuration = CacheExpirationUtil.parseDuration(cacheConfig.getExpireAfterAccess());
      Duration finalDuration =
          CacheExpirationUtil.calculateExpirationWithRandomOffset(baseDuration, cacheConfig);

      builder.expireAfterAccess(finalDuration.getSeconds(), TimeUnit.SECONDS);
    }

    // Configure expireAfterWrite with random offset to prevent cache avalanche
    if (cacheConfig.getExpireAfterWrite() != null) {
      Duration baseDuration = CacheExpirationUtil.parseDuration(cacheConfig.getExpireAfterWrite());
      Duration finalDuration =
          CacheExpirationUtil.calculateExpirationWithRandomOffset(baseDuration, cacheConfig);

      builder.expireAfterWrite(finalDuration.getSeconds(), TimeUnit.SECONDS);

      log.debug(
          "Custom cache TTL: base={}s, final={}s (with random offset)",
          baseDuration.getSeconds(),
          finalDuration.getSeconds());
    }

    return builder;
  }

  @Bean(name = "caffeineCacheBinder")
  @ConditionalOnProperty(prefix = "loadup.cache", name = "type", havingValue = "caffeine")
  public CacheBinder cacheBinder() {
    return new CaffeineCacheBinderImpl();
  }
}
