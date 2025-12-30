package com.github.loadup.components.cache.config;

/*-
 * #%L
 * loadup-components-cache-api
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

