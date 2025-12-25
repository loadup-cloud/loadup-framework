/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.caffeine;

/*-
 * #%L
 * loadup-components-cache-binder-caffeine
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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.caffeine.binder.CaffeineCacheBinderImpl;
import com.github.loadup.components.cache.cfg.LoadUpCacheConfig;
import com.github.loadup.components.cache.config.CacheProperties;
import com.github.loadup.components.cache.util.CacheExpirationUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnClass({Caffeine.class, CaffeineCacheManager.class})
@AutoConfiguration(before = CacheAutoConfiguration.class)
public class CaffeineCacheAutoConfiguration {

    @Resource
    private CacheProperties cacheProperties;

    /**
     * default cache manager with per-cache configurations
     */
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
            cacheConfigs.forEach((cacheName, cacheConfig) -> {
                Caffeine<Object, Object> customCaffeine = buildCustomCaffeine(cacheConfig);
                cacheManager.registerCustomCache(cacheName, customCaffeine);

                log.info("Configured Caffeine cache: name={}, maxSize={}, expireAfterWrite={}",
                    cacheName, cacheConfig.getMaximumSize(), cacheConfig.getExpireAfterWrite());
            });
        }

        return cacheManager;
    }

    /**
     * Build default Caffeine with global configuration
     */
    private Caffeine<Object, Object> buildDefaultCaffeine(CacheProperties.CaffeineConfig config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
            .initialCapacity(config.getInitialCapacity())
            .maximumSize(config.getMaximumSize());

        if (config.getExpireAfterAccessSeconds() > 0) {
            builder.expireAfterAccess(config.getExpireAfterAccessSeconds(), TimeUnit.SECONDS);
        }

        if (config.getExpireAfterWriteSeconds() > 0) {
            // Apply random offset for default cache
            long baseSeconds = config.getExpireAfterWriteSeconds();
            Duration baseDuration = Duration.ofSeconds(baseSeconds);
            Duration finalDuration = CacheExpirationUtil.calculateExpirationWithPercentageOffset(
                baseDuration, 0.1); // 10% random offset

            builder.expireAfterWrite(finalDuration.getSeconds(), TimeUnit.SECONDS);

            log.debug("Default cache TTL: base={}s, final={}s", baseSeconds, finalDuration.getSeconds());
        }

        return builder;
    }

    /**
     * Build custom Caffeine for specific cache with anti-avalanche strategies
     */
    private Caffeine<Object, Object> buildCustomCaffeine(LoadUpCacheConfig cacheConfig) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        // Set maximum size
        if (cacheConfig.getMaximumSize() > 0) {
            builder.maximumSize(cacheConfig.getMaximumSize());
        }

        // Configure expireAfterAccess with random offset
        if (cacheConfig.getExpireAfterAccess() != null) {
            Duration baseDuration = CacheExpirationUtil.parseDuration(cacheConfig.getExpireAfterAccess());
            Duration finalDuration = CacheExpirationUtil.calculateExpirationWithRandomOffset(
                baseDuration, cacheConfig);

            builder.expireAfterAccess(finalDuration.getSeconds(), TimeUnit.SECONDS);
        }

        // Configure expireAfterWrite with random offset to prevent cache avalanche
        if (cacheConfig.getExpireAfterWrite() != null) {
            Duration baseDuration = CacheExpirationUtil.parseDuration(cacheConfig.getExpireAfterWrite());
            Duration finalDuration = CacheExpirationUtil.calculateExpirationWithRandomOffset(
                baseDuration, cacheConfig);

            builder.expireAfterWrite(finalDuration.getSeconds(), TimeUnit.SECONDS);

            log.debug("Custom cache TTL: base={}s, final={}s (with random offset)",
                baseDuration.getSeconds(), finalDuration.getSeconds());
        }

        return builder;
    }

    @Bean(name = "caffeineCacheBinder")
    @ConditionalOnProperty(prefix = "loadup.cache", name = "type", havingValue = "caffeine")
    public CacheBinder cacheBinder() {
        return new CaffeineCacheBinderImpl();
    }
}
