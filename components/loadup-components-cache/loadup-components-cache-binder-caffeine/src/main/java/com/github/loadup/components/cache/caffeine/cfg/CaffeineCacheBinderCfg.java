package com.github.loadup.components.cache.caffeine.cfg;

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

import com.github.loadup.components.cache.cfg.CacheBinderCfg;
import com.github.loadup.framework.api.cfg.BaseBinderCfg;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.checkerframework.checker.index.qual.NonNegative;

import java.time.Duration;

/**
 * Caffeine Binder Configuration
 *
 * <p>This configuration supports two modes:
 *
 * <ol>
 *   <li>Default mode: Reuse Spring Boot's spring.cache.caffeine.spec configuration
 *   <li>Override mode: Use custom loadup.cache.binder.caffeine.* configuration to override defaults
 * </ol>
 *
 * <p>Configuration priority (from high to low):
 *
 * <ol>
 *   <li>loadup.cache.binder.caffeine.* (custom configuration, highest priority)
 *   <li>spring.cache.caffeine.spec (Spring Boot default configuration)
 * </ol>
 *
 * <p>Example configurations:
 *
 * <pre>
 * # Mode 1: Use Spring Boot default configuration
 * spring:
 *   cache:
 *     caffeine:
 *       spec: maximumSize=1000,expireAfterWrite=300s
 *
 * loadup:
 *   cache:
 *     binder: caffeine
 *
 * # Mode 2: Override with custom configuration
 * spring:
 *   cache:
 *     caffeine:
 *       spec: maximumSize=500,expireAfterWrite=10m  # Default
 *
 * loadup:
 *   cache:
 *     binder: caffeine
 *     binder:
 *       caffeine:
 *         spec: maximumSize=2000,expireAfterWrite=30m  # Override
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CaffeineCacheBinderCfg extends CacheBinderCfg {
  public CaffeineCacheBinderCfg() {
    // 在构造函数中修改默认值
    this.setSerializerBeanName(null);
  }

  /**
   * Caffeine cache spec string
   *
   * <p>Format: key1=value1,key2=value2,...
   *
   * <p>Override: loadup.cache.binder.caffeine.spec
   *
   * <p>Default: spring.cache.caffeine.spec
   *
   * <p>Supported parameters:
   *
   * <ul>
   *   <li>initialCapacity - 初始容量
   *   <li>maximumSize - 最大条目数
   *   <li>maximumWeight - 最大权重
   *   <li>expireAfterWrite - 写入后过期时间
   *   <li>expireAfterAccess - 访问后过期时间
   *   <li>weakKeys - 使用弱引用存储键
   *   <li>weakValues - 使用弱引用存储值
   *   <li>softValues - 使用软引用存储值
   *   <li>recordStats - 记录统计信息
   * </ul>
   */
  private String spec;

  private long maximumSize;
  private Duration expireAfterWrite;
  private Duration expireAfterAccess;

    /**
     * 是否开启随机过期
     */
  private boolean enableRandomExpiry = false;
    /**
     * 随机因子 (例如 0.2 表示在 100%~120% 之间波动)
     */
  private double randomFactor = 0.2;

  /**
   * Check if any custom configuration is provided
   *
   * @return true if any custom configuration field is set
   */
  public boolean hasCustomConfig() {
    return spec != null && !spec.isEmpty();
  }
}
