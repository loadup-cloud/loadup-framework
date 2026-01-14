package com.github.loadup.components.cache.redis.binder;

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

import com.github.loadup.components.cache.binder.AbstractCacheBinder;
import com.github.loadup.components.cache.binder.CacheBinder;
import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.redis.cfg.RedisCacheBinderCfg;
import com.github.loadup.framework.api.manager.ConfigurationResolver;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StringUtils;

/**
 * Redis Cache Binder Implementation
 *
 * <p>This binder provides Redis cache operations and supports flexible configuration:
 *
 * <ol>
 *   <li>Default: Uses Spring Boot's spring.data.redis configuration
 *   <li>Custom: Uses loadup.cache.redis configuration to override defaults
 * </ol>
 *
 * @see RedisCacheBinderCfg
 */
@Slf4j
public class RedisCacheBinder extends AbstractCacheBinder<RedisCacheBinderCfg, CacheBindingCfg> {
  @Autowired private RedisProperties springRedisProperties; // 注入 YAML 中的 spring.redis 配置

  @Autowired(required = false)
  private RedisConnectionFactory defaultFactory; // 注入 Spring 自动创建的默认工厂

  private RedisTemplate<String, byte[]> redisTemplate;

  @Override
  protected void onInit() {
    // 1. 决定使用哪个连接工厂
    RedisConnectionFactory factory = resolveConnectionFactory();

    // 2. 初始化 RedisTemplate
    this.redisTemplate = new RedisTemplate<>();
    this.redisTemplate.setConnectionFactory(factory);
    this.redisTemplate.setKeySerializer(RedisSerializer.string());
    this.redisTemplate.setValueSerializer(RedisSerializer.byteArray());
    this.redisTemplate.afterPropertiesSet();
  }

  private RedisConnectionFactory resolveConnectionFactory() {
    // 判断当前 Binder 的配置是否与 Spring Boot 默认配置完全一致
    if (isMatchDefaultConfig() && defaultFactory != null) {
      log.info("RedisBinder [{}] 配置与全局一致，复用默认 RedisConnectionFactory", name);
      return defaultFactory;
    }

    // 如果不一致（比如换了 DB 或者 Host），则创建一个新的私有工厂
    log.info("RedisBinder [{}] 配置了独立参数，正在创建新的 LettuceConnectionFactory", name);
    return createPrivateFactory();
  }

  /** 判断配置是否与 spring.redis 一致 */
  private boolean isMatchDefaultConfig() {
    // 如果 Binder 没有配置 Host，或者配置的 Host 和 DB 与 Spring 默认的一样
    boolean dbMatch =
        binderCfg.getDatabase() == 0
            || binderCfg.getDatabase() == springRedisProperties.getDatabase();
    return dbMatch;
  }

  /** 创建独立的工厂 */
  private RedisConnectionFactory createPrivateFactory() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();

    // 合并配置：优先用 BinderCfg，为空则用 RedisProperties
    String host =
        ConfigurationResolver.resolve(binderCfg.getHost(), springRedisProperties.getHost());
    int port = binderCfg.getPort() != 0 ? binderCfg.getPort() : springRedisProperties.getPort();
    int finalDb =
        ConfigurationResolver.resolve(
            bindingCfg.getDatabase(),
            binderCfg.getDatabase(),
            springRedisProperties.getDatabase());
    String pwd =
        ConfigurationResolver.resolve(binderCfg.getPassword(), springRedisProperties.getPassword());

    config.setHostName(host);
    config.setPort(port);
    config.setDatabase(finalDb);
    if (StringUtils.hasText(pwd)) {
      config.setPassword(pwd);
    }

    LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
    // 注意：必须调用 afterPropertiesSet 触发初始化
    factory.afterPropertiesSet();
    return factory;
  }

  @Override
  public String getBinderType() {
    return "redis";
  }

  @Override
  public boolean set(String key, Object value) {
    if (value == null) return false;

    // 核心：调用父类的 wrapValue。
    // 因为 AbstractCacheBinder 注入了 serializer，wrapValue 会返回 byte[]
    Object wrapped = wrapValue(value);

    if (wrapped instanceof byte[]) {
      redisTemplate.opsForValue().set(key, (byte[]) wrapped);
    } else {
      log.warn("RedisBinder [{}] 未配置序列化器，无法存储非字节数据", name);
    }
    return true;
  }

  @Override
  public Object get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public boolean delete(String key) {
    redisTemplate.delete(key);
    return true;
  }

  @Override
  public boolean deleteAll(Collection<String> keys) {
    redisTemplate.delete(keys);
    return true;
  }

  @Override
  public void cleanUp() {}

  @Override
  public void afterDestroy() {
    // 只有当我们自己 new 了工厂时，才需要手动销毁
    // 如果是复用的 defaultFactory，Spring 容器会自动管理它的生命周期
    if (this.redisTemplate.getConnectionFactory() instanceof LettuceConnectionFactory) {
      LettuceConnectionFactory factory =
          (LettuceConnectionFactory) this.redisTemplate.getConnectionFactory();
      if (factory != defaultFactory) { // 关键判断：不是默认的才销毁
        factory.destroy();
        log.info("私有 RedisConnectionFactory [{}] 已销毁", name);
      }
    }
  }
}
