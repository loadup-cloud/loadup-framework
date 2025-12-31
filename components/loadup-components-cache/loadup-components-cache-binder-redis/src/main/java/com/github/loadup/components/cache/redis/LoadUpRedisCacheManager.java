package com.github.loadup.components.cache.redis;

/*-
 * #%L
 * loadup-components-cache-binder-redis
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

import org.springframework.data.redis.cache.*;
import org.springframework.util.StringUtils;

public class LoadUpRedisCacheManager extends RedisCacheManager {
  private final RedisCacheConfiguration defaultConfig;

  public LoadUpRedisCacheManager(
      RedisCacheWriter cacheWriter,
      RedisCacheConfiguration defaultCacheConfiguration,
      Map<String, RedisCacheConfiguration> initialCacheConfigurations) {
    super(cacheWriter, defaultCacheConfiguration, initialCacheConfigurations);
    this.defaultConfig = defaultCacheConfiguration;
  }

  /**
   * name#ttl test#60S key#60M
   *
   * @return
   */
  @Override
  protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
    // 在动态创建缓存时，复用默认配置
    if (cacheConfig == null) {
      cacheConfig = defaultConfig;
    }

    String[] array = StringUtils.delimitedListToStringArray(name, "#");
    name = array[0];
    // 解析TTL
    if (array.length > 1) {
      String duration = array[1];
      if (!StringUtils.startsWithIgnoreCase(duration, "PT")) {
        duration = "PT" + duration;
      }
      cacheConfig = cacheConfig.entryTtl(Duration.parse(duration));
    }
    return super.createRedisCache(name, cacheConfig);
  }
}
