package io.github.loadup.components.cache.redis.cfg;

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

import io.github.loadup.components.cache.cfg.CacheBinderCfg;
import io.github.loadup.components.cache.constants.CacheConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Redis Binder Configuration
 *
 * <p>This configuration supports two modes:
 *
 * <ol>
 *   <li>Default mode: Reuse Spring Boot's spring.data.redis configuration
 *   <li>Override mode: Use custom loadup.cache.binder.redis.* configuration to override defaults
 * </ol>
 *
 * <p>Configuration priority (from high to low):
 *
 * <ol>
 *   <li>loadup.cache.binder.redis.* (custom configuration, highest priority)
 *   <li>spring.data.redis.* (Spring Boot default configuration)
 * </ol>
 *
 * <p>Example configurations:
 *
 * <pre>
 * # Mode 1: Use Spring Boot default configuration
 * spring:
 *   data:
 *     redis:
 *       host: localhost
 *       port: 6379
 *       password: secret
 *
 * loadup:
 *   cache:
 *     binder: redis
 *
 * # Mode 2: Override with custom configuration
 * spring:
 *   data:
 *     redis:
 *       host: localhost  # Default config
 *
 * loadup:
 *   cache:
 *     binder: redis
 *     binder:
 *       redis:
 *         host: redis-cache.example.com  # Override
 *         port: 6380
 *         password: custom-secret
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RedisCacheBinderCfg extends CacheBinderCfg {
  /** 是否开启随机过期 */
  private boolean enableRandomExpiry = false;

  /** 随机因子 (例如 0.2 表示在 100%~120% 之间波动) */
  private double randomFactor = 0.2;

  /**
   * Redis database index (default: 0) Override: loadup.cache.binder.redis.database Default:
   * spring.data.redis.database
   */
  private Integer database;

  /** Redis server host Override: loadup.cache.binder.redis.host Default: spring.data.redis.host */
  private String host;

  /**
   * Redis server password Override: loadup.cache.binder.redis.password Default:
   * spring.data.redis.password
   */
  private String password;

  /**
   * Redis server port (default: 6379) Override: loadup.cache.binder.redis.port Default:
   * spring.data.redis.port
   */
  private Integer port;

  /**
   * Redis server username (for Redis 6+ ACL) Override: loadup.cache.binder.redis.username Default:
   * spring.data.redis.username
   */
  private String username;

  /**
   * Check if any custom configuration is provided
   *
   * @return true if any custom configuration field is set
   */
  public boolean hasCustomConfig() {
    return host != null || port != null || password != null || username != null || database != null;
  }

  public RedisCacheBinderCfg() {
    // Redis 驱动默认建议使用 JSON 序列化，方便跨语言查看
    setSerializerBeanName(CacheConstants.SERIALIZER_JSON);
  }
}
