package com.github.loadup.components.cache.redis.cfg;

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

import com.github.loadup.framework.api.cfg.BaseBinderCfg;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
@ConfigurationProperties(prefix = "loadup.cache.binder.redis")
public class RedisBinderCfg extends BaseBinderCfg {

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
   * Connection timeout in milliseconds (default: 2000) Override:
   * loadup.cache.binder.redis.connect-timeout Default: spring.data.redis.connect-timeout
   */
  private Long connectTimeout;

  /**
   * Read timeout in milliseconds (default: 2000) Override: loadup.cache.binder.redis.read-timeout
   * Default: spring.data.redis.timeout
   */
  private Long readTimeout;

  /**
   * Redis deployment pattern: standalone, sentinel, cluster Override:
   * loadup.cache.binder.redis.pattern Default: auto-detect based on spring.data.redis configuration
   */
  private String pattern;

  // ========== Sentinel Configuration ==========

  /**
   * Sentinel master name Override: loadup.cache.binder.redis.sentinel-master Default:
   * spring.data.redis.sentinel.master
   */
  private String sentinelMaster;

  /**
   * Sentinel nodes (format: host:port,host:port) Override: loadup.cache.binder.redis.sentinel-nodes
   * Default: spring.data.redis.sentinel.nodes
   */
  private String[] sentinelNodes;

  /**
   * Sentinel password Override: loadup.cache.binder.redis.sentinel-password Default:
   * spring.data.redis.sentinel.password
   */
  private String sentinelPassword;

  // ========== Cluster Configuration ==========

  /**
   * Cluster nodes (format: host:port,host:port) Override: loadup.cache.binder.redis.cluster-nodes
   * Default: spring.data.redis.cluster.nodes
   */
  private String[] clusterNodes;

  /**
   * Max redirects for cluster (default: 3) Override: loadup.cache.binder.redis.max-redirects
   * Default: spring.data.redis.cluster.max-redirects
   */
  private Integer maxRedirects;

  // ========== Connection Pool Configuration ==========

  /**
   * Maximum number of connections in the pool (default: 8) Override:
   * loadup.cache.binder.redis.max-active Default: spring.data.redis.lettuce.pool.max-active
   */
  private Integer maxActive;

  /**
   * Maximum wait time for connection from pool in milliseconds (default: -1, no limit) Override:
   * loadup.cache.binder.redis.max-wait Default: spring.data.redis.lettuce.pool.max-wait
   */
  private Long maxWait;

  /**
   * Maximum idle connections in pool (default: 8) Override: loadup.cache.binder.redis.max-idle
   * Default: spring.data.redis.lettuce.pool.max-idle
   */
  private Integer maxIdle;

  /**
   * Minimum idle connections in pool (default: 0) Override: loadup.cache.binder.redis.min-idle
   * Default: spring.data.redis.lettuce.pool.min-idle
   */
  private Integer minIdle;

  /**
   * Whether SSL is enabled Override: loadup.cache.binder.redis.ssl-enabled Default:
   * spring.data.redis.ssl.enabled
   */
  private Boolean sslEnabled;

  /**
   * Check if any custom configuration is provided
   *
   * @return true if any custom configuration field is set
   */
  public boolean hasCustomConfig() {
    return host != null
        || port != null
        || password != null
        || username != null
        || database != null
        || connectTimeout != null
        || readTimeout != null
        || pattern != null
        || sentinelMaster != null
        || sentinelNodes != null
        || sentinelPassword != null
        || clusterNodes != null
        || maxRedirects != null
        || maxActive != null
        || maxWait != null
        || maxIdle != null
        || minIdle != null
        || sslEnabled != null;
  }
}
