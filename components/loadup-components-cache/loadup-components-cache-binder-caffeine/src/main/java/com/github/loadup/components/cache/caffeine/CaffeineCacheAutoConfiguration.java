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
import com.github.loadup.commons.util.date.DurationUtils;
import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.caffeine.binder.CaffeineCacheBinderImpl;
import com.github.loadup.components.cache.caffeine.cfg.LoadUpCaffeineCacheProperties;
import com.github.loadup.components.cache.cfg.LoadUpCacheConfig;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnClass({Caffeine.class, CaffeineCacheManager.class})
@AutoConfiguration(before = CacheAutoConfiguration.class)
@ConditionalOnMissingBean(CacheBinder.class)
public class CaffeineCacheAutoConfiguration {
    @Resource
    private LoadUpCaffeineCacheProperties caffeineCacheProperties;

    /**
     * default cache
     */
    @Primary
    @Bean(name = "caffeineCacheManager")
    public CacheManager defaultCacheManager() {
        CaffeineCacheManager defaultCacheManager = new CaffeineCacheManager();
        defaultCacheManager.setAllowNullValues(caffeineCacheProperties.getAllowNullValue());
        // 设置默认的缓存配置
        defaultCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS) // 默认过期时间
                .maximumSize(100)); // 默认最大缓存大小

        return defaultCacheManager;
    }

    private Caffeine fetchCaffeine(String cacheName) {
        Map<String, LoadUpCacheConfig> cacheConfig = caffeineCacheProperties.getCacheConfig();
        Caffeine<Object, Object> caffeine = null;
        for (Map.Entry<String, LoadUpCacheConfig> entry : cacheConfig.entrySet()) {
            String key = entry.getKey();
            LoadUpCacheConfig config = entry.getValue();
            if (StringUtils.equals(key, cacheName)) {
                caffeine = Caffeine.newBuilder()
                        .expireAfterWrite(DurationUtils.parse(config.getExpireAfterWrite())) // 设置缓存过期时间
                        .maximumSize(config.getMaximumSize());
            }
        }
        return caffeine;
    }

    @Bean(name = "caffeineCacheBinder")
    @ConditionalOnMissingBean(CacheBinder.class)
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
    public CacheBinder cacheBinder() {
        return new CaffeineCacheBinderImpl();
    }
}
