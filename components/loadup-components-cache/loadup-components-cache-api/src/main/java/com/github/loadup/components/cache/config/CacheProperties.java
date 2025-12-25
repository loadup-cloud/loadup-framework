/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.config;

/*-
 * #%L
 * loadup-components-cache-api
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

import com.github.loadup.components.cache.cfg.LoadUpCacheConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * LoadUp Cache Configuration Properties
 * Unified configuration for all cache implementations
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "loadup.cache")
public class CacheProperties {

    /**
     * Cache type to use. Supported values: redis, caffeine
     */
    private String type = "caffeine";

    /**
     * Redis specific configuration
     */
    private RedisConfig redis = new RedisConfig();

    /**
     * Caffeine specific configuration
     */
    private CaffeineConfig caffeine = new CaffeineConfig();

    /**
     * Redis configuration
     */
    @Getter
    @Setter
    public static class RedisConfig {
        /**
         * Redis server host
         */
        private String host = "localhost";

        /**
         * Redis server port
         */
        private int port = 6379;

        /**
         * Redis password
         */
        private String password;

        /**
         * Redis database index
         */
        private int database = 0;

        /**
         * Per-cache configurations for Redis
         */
        private Map<String, LoadUpCacheConfig> cacheConfig = new HashMap<>();
    }

    /**
     * Caffeine configuration
     */
    @Getter
    @Setter
    public static class CaffeineConfig {
        /**
         * Initial cache capacity
         */
        private int initialCapacity = 1000;

        /**
         * Maximum cache size
         */
        private long maximumSize = 5000;

        /**
         * Expire after access duration in seconds
         */
        private long expireAfterAccessSeconds = 600;

        /**
         * Expire after write duration in seconds
         */
        private long expireAfterWriteSeconds = 1200;

        /**
         * Allow null values in cache
         */
        private boolean allowNullValue = false;

        /**
         * Per-cache configurations for Caffeine
         */
        private Map<String, LoadUpCacheConfig> cacheConfig = new HashMap<>();
    }
}

