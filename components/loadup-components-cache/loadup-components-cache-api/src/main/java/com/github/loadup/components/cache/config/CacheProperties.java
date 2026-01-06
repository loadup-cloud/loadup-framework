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
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** LoadUp Cache Configuration Properties. Unified configuration for all cache implementations. */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "loadup.cache")
public class CacheProperties {

  /** Cache type to use. Supported values: redis, caffeine */
  private CacheType type = CacheType.CAFFEINE;

  /** Redis specific configuration */
  private RedisConfig redis = new RedisConfig();

  /** Caffeine specific configuration */
  private CaffeineConfig caffeine = new CaffeineConfig();

  /** Supported cache types */
  public enum CacheType {
    REDIS,
    CAFFEINE;

    @Override
    public String toString() {
      return name().toLowerCase();
    }

    /**
     * Parse a text value (case-insensitive) to a CacheType. Useful when binding from
     * properties/yaml.
     */
    @SuppressWarnings("unused")
    public static CacheType fromString(String value) {
      if (value == null) return null;
      String normalized = value.trim().toUpperCase();
      for (CacheType t : values()) {
        if (t.name().equalsIgnoreCase(normalized) || t.toString().equalsIgnoreCase(value)) {
          return t;
        }
      }
      throw new IllegalArgumentException("Unknown cache type: " + value);
    }
  }

  /** Redis cache configuration */
  @Getter
  @Setter
  public static class RedisConfig {
    /** Per-cache configurations for Redis. Use spring.redis.* for connection settings. */
    private Map<String, LoadUpCacheConfig> cacheConfig;
  }

  /** Caffeine cache configuration */
  @Getter
  @Setter
  public static class CaffeineConfig {
    /** Per-cache configurations for Caffeine. Use spring.cache.caffeine.* for default settings. */
    private Map<String, LoadUpCacheConfig> cacheConfig;
  }
}
