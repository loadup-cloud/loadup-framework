package io.github.loadup.components.dfs.local.cfg;

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

import io.github.loadup.components.dfs.cfg.DfsBinderCfg;
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
public class LocalDfsBinderCfg extends DfsBinderCfg {
  private String basePath = System.getProperty("user.home") + "/dfs-storage";
}
