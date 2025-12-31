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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import com.github.benmanes.caffeine.cache.Caffeine;

/** Extended Caffeine Cache Manager with per-cache custom configurations */
public class LoadUpCaffeineCacheManager extends CaffeineCacheManager {

  private final Map<String, Caffeine<Object, Object>> customCaffeineSpecs =
      new ConcurrentHashMap<>();

  /**
   * Register a custom Caffeine configuration for a specific cache
   *
   * @param cacheName Cache name
   * @param caffeine Custom Caffeine builder
   */
  public void registerCustomCache(String cacheName, Caffeine<Object, Object> caffeine) {
    customCaffeineSpecs.put(cacheName, caffeine);
  }

  @Override
  protected Cache createCaffeineCache(String name) {
    // Check if there's a custom configuration for this cache
    Caffeine<Object, Object> customCaffeine = customCaffeineSpecs.get(name);

    if (customCaffeine != null) {
      // Use custom configuration
      return new CaffeineCache(name, customCaffeine.build(), isAllowNullValues());
    }

    // Fall back to default configuration
    return super.createCaffeineCache(name);
  }
}
