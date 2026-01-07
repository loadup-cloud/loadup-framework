package com.github.loadup.components.cache.caffeine.config;

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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.caffeine.binder.CaffeineCacheBinder;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.cfg.CacheConfigs;
import com.github.loadup.components.cache.util.CacheExpirationUtil;
import com.github.loadup.framework.api.enums.BinderEnum;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@EnableCaching
@EnableConfigurationProperties(CacheBindingCfg.class)
@ConditionalOnClass({Caffeine.class, CaffeineCacheManager.class})
@AutoConfiguration(before = CacheAutoConfiguration.class)
@ConditionalOnProperty(prefix = "loadup.cache", name = "binder", havingValue = "caffeine")
public class CaffeineCacheAutoConfiguration {

  @Resource private CacheBindingCfg cacheBindingCfg;

  @Value("${spring.cache.caffeine.spec:}")
  private String cacheSpec;

  /** default cache manager with per-cache configurations */
  @Primary
  @Bean(name = "caffeineCacheManager")
  @ConditionalOnProperty(prefix = "loadup.cache", name = "binder", havingValue = "caffeine")
  public CacheManager defaultCacheManager() {
    LoadUpCaffeineCacheManager cacheManager = new LoadUpCaffeineCacheManager();

    // Set default configuration from spring.cache.caffeine.spec
    if (cacheSpec != null && !cacheSpec.isEmpty()) {
      cacheManager.setCacheSpecification(cacheSpec);
      log.info("Applied default Caffeine cache spec: {}", cacheSpec);
    }

    // Support new unified configuration structure - these will override the default spec
    Map<String, CacheConfigs> cacheConfigs = getCacheConfigs();

    if (cacheConfigs != null && !cacheConfigs.isEmpty()) {
      cacheConfigs.forEach(
          (cacheName, cacheConfig) -> {
            // Only configure Caffeine caches (check binder selection)
            if (cacheBindingCfg.getBinderForCache(cacheName) == BinderEnum.CacheBinder.CAFFEINE) {
              Caffeine<Object, Object> customCaffeine = buildCustomCaffeine(cacheConfig);
              cacheManager.registerCustomCache(cacheName, customCaffeine);

              log.info(
                  "Configured custom Caffeine cache: name={}, maxSize={}, expireAfterWrite={} (overrides default spec)",
                  cacheName,
                  cacheConfig.getMaximumSize(),
                  cacheConfig.getExpireAfterWrite());
            }
          });
    }

    return cacheManager;
  }

  /**
   * Get cache configurations supporting both new and legacy structure Priority: new cacheConfigs
   * map > legacy caffeine.cacheConfig
   */
  private Map<String, CacheConfigs> getCacheConfigs() {
    // Try new structure first
    if (cacheBindingCfg.getCacheConfigs() != null && !cacheBindingCfg.getCacheConfigs().isEmpty()) {
      return cacheBindingCfg.getCacheConfigs();
    }
    return new HashMap<>();
  }

  /** Build custom Caffeine for specific cache with anti-avalanche strategies */
  private Caffeine<Object, Object> buildCustomCaffeine(CacheConfigs cacheConfig) {
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
  @ConditionalOnProperty(prefix = "loadup.cache", name = "binder", havingValue = "caffeine")
  public CacheBinder cacheBinder() {
    return new CaffeineCacheBinder();
  }
}
