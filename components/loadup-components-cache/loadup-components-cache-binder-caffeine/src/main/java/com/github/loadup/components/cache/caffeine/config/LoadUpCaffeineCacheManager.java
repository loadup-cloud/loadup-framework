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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * Extended Caffeine Cache Manager with per-cache custom configurations
 *
 * <p>This cache manager extends Spring's CaffeineCacheManager to support:
 *
 * <ul>
 *   <li>Default configuration from spring.cache.caffeine.spec
 *   <li>Per-cache custom configurations that override the default
 * </ul>
 */
public class LoadUpCaffeineCacheManager extends CaffeineCacheManager {

  private final Map<String, Caffeine<Object, Object>> customCaffeineSpecs =
      new ConcurrentHashMap<>();

  /**
   * Register a custom Caffeine configuration for a specific cache
   *
   * <p>This will override the default spring.cache.caffeine.spec for the specified cache name.
   *
   * @param cacheName Cache name
   * @param caffeine Custom Caffeine builder
   */
  public void registerCustomCache(String cacheName, Caffeine<Object, Object> caffeine) {
    customCaffeineSpecs.put(cacheName, caffeine);
  }

  /**
   * Get all registered custom cache configurations
   *
   * @return Map of cache name to Caffeine builder
   */
  public Map<String, Caffeine<Object, Object>> getCustomCaffeineSpecs() {
    return customCaffeineSpecs;
  }

  @Override
  protected Cache createCaffeineCache(String name) {
    // Check if there's a custom configuration for this cache
    Caffeine<Object, Object> customCaffeine = customCaffeineSpecs.get(name);

    if (customCaffeine != null) {
      // Use custom configuration (overrides spring.cache.caffeine.spec)
      return new CaffeineCache(name, customCaffeine.build(), isAllowNullValues());
    }

    // Fall back to default configuration from spring.cache.caffeine.spec
    // This will use the CaffeineSpec set via setCacheSpecification() or setCaffeine()
    return super.createCaffeineCache(name);
  }
}
