package com.github.loadup.components.cache.starter.config;

import com.github.loadup.components.cache.binder.CacheTicker;
import com.github.loadup.components.cache.caffeine.binder.CaffeineCacheBinder;
import com.github.loadup.components.cache.caffeine.binding.CaffeineCacheBinding;
import com.github.loadup.components.cache.caffeine.cfg.CaffeineCacheBinderCfg;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.constants.CacheConstants;
import com.github.loadup.components.cache.redis.binder.RedisCacheBinder;
import com.github.loadup.components.cache.redis.binding.RedisCacheBinding;
import com.github.loadup.components.cache.redis.cfg.RedisCacheBinderCfg;
import com.github.loadup.components.cache.serializer.CacheSerializer;
import com.github.loadup.components.cache.serializer.JsonCacheSerializer;
import com.github.loadup.components.cache.serializer.KryoCacheSerializer;
import com.github.loadup.components.cache.starter.manager.CacheBindingManager;
import com.github.loadup.components.cache.starter.properties.CacheGroupProperties;
import com.github.loadup.framework.api.core.BindingPostProcessor;
import com.github.loadup.framework.api.manager.BindingMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

  @Bean(name = CacheConstants.SERIALIZER_KRYO)
  @ConditionalOnMissingBean(name = CacheConstants.SERIALIZER_KRYO)
  public CacheSerializer customKryoSerializer() {
    return new KryoCacheSerializer();
  }

  /** S3 模块的自动注册逻辑 只有当 classpath 下存在 CaffeineCacheBinding 类时（即引入了 binder-s3 依赖），这段逻辑才生效 */
  @Configuration
  @ConditionalOnClass(
      name = {
        "com.github.benmanes.caffeine.cache.Caffeine",
        "com.github.loadup.components.cache.caffeine.binding.CaffeineCacheBinding"
      })
  static class CaffeineBindingRegistry {
    // Spring 会自动注入上面定义的 bindingManager
    public CaffeineBindingRegistry(CacheBindingManager bindingManager) {
      bindingManager.register(
          "caffeine",
          new BindingMetadata<>(
              CacheBindingCfg.class,
              CaffeineCacheBinderCfg.class,
              CaffeineCacheBinder.class,
              ctx -> new CaffeineCacheBinding()));
    }
  }

  /** Redis 模块的自动注册逻辑 */
  @Configuration
  @ConditionalOnClass(name = "com.github.loadup.components.cache.redis.binding.RedisCacheBinding")
  static class RedisBindingRegistry {
    public RedisBindingRegistry(CacheBindingManager bindingManager) {
      bindingManager.register(
          "redis",
          new BindingMetadata<>(
              CacheBindingCfg.class,
              RedisCacheBinderCfg.class,
              RedisCacheBinder.class,
              ctx -> new RedisCacheBinding()));
    }
  }
}
