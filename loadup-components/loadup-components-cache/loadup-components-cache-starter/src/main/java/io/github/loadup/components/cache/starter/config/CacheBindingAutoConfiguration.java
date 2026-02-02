package io.github.loadup.components.cache.starter.config;

/*-
 * #%L
 * Loadup Cache Starter
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.components.cache.binder.CacheTicker;
import io.github.loadup.components.cache.binding.impl.DefaultCacheBinding;
import io.github.loadup.components.cache.caffeine.binder.CaffeineCacheBinder;
import io.github.loadup.components.cache.caffeine.cfg.CaffeineCacheBinderCfg;
import io.github.loadup.components.cache.cfg.CacheBindingCfg;
import io.github.loadup.components.cache.constants.CacheConstants;
import io.github.loadup.components.cache.redis.binder.RedisCacheBinder;
import io.github.loadup.components.cache.redis.cfg.RedisCacheBinderCfg;
import io.github.loadup.components.cache.serializer.CacheSerializer;
import io.github.loadup.components.cache.serializer.JsonCacheSerializer;
import io.github.loadup.components.cache.starter.manager.CacheBindingManager;
import io.github.loadup.components.cache.starter.properties.CacheGroupProperties;
import io.github.loadup.framework.api.core.BindingPostProcessor;
import io.github.loadup.framework.api.manager.BindingMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CacheGroupProperties.class)
public class CacheBindingAutoConfiguration {
  /** 将 CacheBindingManager 定义为一个单例 Bean 依赖的 props 和 context 会由 Spring 自动注入到方法参数中 */
  @Bean
  @ConditionalOnMissingBean
  public CacheBindingManager cacheBindingManager(
      CacheGroupProperties props, ApplicationContext context) {
    return new CacheBindingManager(props, context);
  }

  @Bean
  public BindingPostProcessor bindingPostProcessor(ApplicationContext context) {
    // 显式传入 context
    return new BindingPostProcessor(context);
  }

  /** 注册默认的时间源 使用 @ConditionalOnMissingBean 允许用户提供自己的实现来覆盖它 */
  @Bean(name = CacheConstants.DEFAULT_TICKER)
  @ConditionalOnMissingBean(name = CacheConstants.DEFAULT_TICKER)
  public CacheTicker defaultCacheTicker() {
    return CacheTicker.SYSTEM;
  }

  /** 注册默认的 JSON 序列化器 使用 @ConditionalOnMissingBean 允许用户提供自己的实现来覆盖它 */
  @Bean(name = CacheConstants.SERIALIZER_JSON)
  @ConditionalOnMissingBean(CacheSerializer.class)
  public CacheSerializer defaultJsonCacheSerializer() {
    return new JsonCacheSerializer();
  }

  /** 当 classpath 中存在 Caffeine 时，注册其元数据 */
  @Bean
  @ConditionalOnClass(name = "com.github.benmanes.caffeine.cache.Cache")
  public BindingMetadata<?, ?, ?, ?> caffeineMetadata() {
    return new BindingMetadata<>(
        "caffeine",
        DefaultCacheBinding.class,
        CaffeineCacheBinder.class,
        CacheBindingCfg.class,
        CaffeineCacheBinderCfg.class,
        ctx -> new DefaultCacheBinding());
  }

  /** 当 classpath 中存在 Redis 时，注册其元数据 */
  @Bean
  @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
  public BindingMetadata<?, ?, ?, ?> redisMetadata() {
    return new BindingMetadata<>(
        "redis",
        DefaultCacheBinding.class,
        RedisCacheBinder.class,
        CacheBindingCfg.class,
        RedisCacheBinderCfg.class,
        ctx -> new DefaultCacheBinding());
  }
}
