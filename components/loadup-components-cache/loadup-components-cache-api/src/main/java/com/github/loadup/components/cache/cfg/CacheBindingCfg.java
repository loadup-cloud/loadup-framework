package com.github.loadup.components.cache.cfg;

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

import com.github.loadup.framework.api.cfg.BaseBindingCfg;
import com.github.loadup.framework.api.enums.BinderEnum;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * LoadUp Cache Configuration Properties. Unified configuration for all cache implementations with
 * per-cache-name binder selection.
 *
 * <p>Configuration examples:
 *
 * <pre>
 * # Global default binder
 * loadup.cache.binder=redis
 *
 * # Per-cache-name binder selection
 * loadup.cache.binders.userCache=redis
 * loadup.cache.binders.productCache=caffeine
 *
 * # Per-cache-name common configurations
 * loadup.cache.userCache.maximumSize=10000
 * loadup.cache.userCache.expireAfterWrite=30m
 * loadup.cache.productCache.maximumSize=5000
 * loadup.cache.productCache.expireAfterWrite=1h
 * </pre>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "loadup.cache")
public class CacheBindingCfg extends BaseBindingCfg {
  /** binder name */
  private BinderEnum.CacheBinder binder;

  /**
   * Per-cache-name binder mapping
   *
   * <p>Examples:
   *
   * <pre>
   * loadup.cache.binders.userCache=redis
   * loadup.cache.binders.productCache=caffeine
   * </pre>
   */
  private Map<String, BinderEnum.CacheBinder> binders = new HashMap<>();

  /**
   * Per-cache-name common configurations These are shared properties that work across all cache
   * implementations
   *
   * <p>Examples:
   *
   * <pre>
   * loadup.cache.userCache.maximumSize=10000
   * loadup.cache.userCache.expireAfterWrite=30m
   * </pre>
   */
  @NestedConfigurationProperty private Map<String, CacheConfigs> cacheConfigs = new HashMap<>();

  /**
   * Get the binder type for a specific cache name
   *
   * @param cacheName the cache name
   * @return the binder type (falls back to global binder if not specified)
   */
  public BinderEnum.CacheBinder getBinderForCache(String cacheName) {
    BinderEnum.CacheBinder binderType = binders.get(cacheName);
    if (binderType != null) {
      return binderType;
    }
    return binder != null ? binder : BinderEnum.CacheBinder.CAFFEINE;
  }

  /**
   * Get the configuration for a specific cache name
   *
   * @param cacheName the cache name
   * @return the cache configuration, or null if not configured
   */
  public CacheConfigs getCacheConfig(String cacheName) {
    return cacheConfigs.get(cacheName);
  }
}
