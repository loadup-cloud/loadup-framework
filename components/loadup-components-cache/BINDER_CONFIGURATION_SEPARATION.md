# Binder 配置分离实现总结

## 实现目标

将 binder 的配置按照不同的 binder 类型分开：

- `loadup.cache.binder.redis.*` - Redis binder 专属配置
- `loadup.cache.binder.caffeine.*` - Caffeine binder 专属配置

## 修改的文件

### 1. CacheBindingCfg.java

**路径**: `loadup-components-cache-api/src/main/java/com/github/loadup/components/cache/cfg/`

**变更**:

- 更新文档注释，添加新的配置示例
- 说明 binder 特定配置的路径

**配置示例**:

```yaml
loadup:
  cache:
    binder: redis
    cache-configs:
      userCache:
        expire-after-write: 30m
    binder:
      redis:
        host: localhost
        port: 6379
      caffeine:
        spec: maximumSize=1000
```

### 2. RedisBinderCfg.java

**路径**: `loadup-components-cache-binder-redis/src/main/java/com/github/loadup/components/cache/redis/cfg/`

**变更**:

- 更新所有字段注释，配置路径从 `loadup.cache.redis.*` 改为 `loadup.cache.binder.redis.*`
- 更新类文档注释中的配置示例

**配置路径变更**:

```yaml
# 旧路径
loadup:
  cache:
    redis:
      host: localhost

# 新路径
loadup:
  cache:
    binder:
      redis:
        host: localhost
```

### 3. RedisCacheAutoConfiguration.java

**路径**: `loadup-components-cache-binder-redis/src/main/java/com/github/loadup/components/cache/redis/config/`

**变更**:

- `redisBinderCfg()` Bean 的 `@ConfigurationProperties` 前缀从 `loadup.cache.redis` 改为 `loadup.cache.binder.redis`

```java

@Bean
@ConfigurationProperties(prefix = "loadup.cache.binder.redis")
public RedisBinderCfg redisBinderCfg() {
    return new RedisBinderCfg();
}
```

### 4. RedisConnectionFactoryConfiguration.java

**路径**: `loadup-components-cache-binder-redis/src/main/java/com/github/loadup/components/cache/redis/config/`

**变更**:

- `@ConditionalOnProperty` 从 `loadup.cache.redis` 改为 `loadup.cache.binder.redis`
- 日志输出更新，提示使用新的配置路径

```java

@Bean
@ConditionalOnProperty(prefix = "loadup.cache.binder.redis", name = "host")
public RedisConnectionFactory customRedisConnectionFactory() {
    log.info("Creating custom RedisConnectionFactory with loadup.cache.binder.redis configuration");
    // ...
}
```

### 5. CaffeineBinderCfg.java (新增)

**路径**: `loadup-components-cache-binder-caffeine/src/main/java/com/github/loadup/components/cache/caffeine/cfg/`

**内容**:

- 新增 Caffeine binder 专属配置类
- 支持 `spec` 字段配置 Caffeine cache spec
- 提供 `hasCustomConfig()` 方法判断是否有自定义配置

```java

@Data
@EqualsAndHashCode(callSuper = true)
public class CaffeineBinderCfg extends BaseBinderCfg {
    private String spec;

    public boolean hasCustomConfig() {
        return spec != null && !spec.isEmpty();
    }
}
```

### 6. CaffeineCacheAutoConfiguration.java

**路径**: `loadup-components-cache-binder-caffeine/src/main/java/com/github/loadup/components/cache/caffeine/config/`

**变更**:

- 添加 `CaffeineBinderCfg` Bean，配置前缀为 `loadup.cache.binder.caffeine`
- 更新 `defaultCacheManager()` 方法，支持配置优先级：
    1. `loadup.cache.binder.caffeine.spec` (最高优先级)
    2. `spring.cache.caffeine.spec` (默认)
- 添加日志输出，明确标识使用的配置来源

```java

@Bean
@ConfigurationProperties(prefix = "loadup.cache.binder.caffeine")
public CaffeineBinderCfg caffeineBinderCfg() {
    return new CaffeineBinderCfg();
}

public CacheManager defaultCacheManager(CaffeineBinderCfg caffeineBinderCfg) {
    String effectiveSpec = caffeineBinderCfg.hasCustomConfig()
            ? caffeineBinderCfg.getSpec()
            : springCacheSpec;
    // ...
}
```

## 新增的文档

### 1. BINDER_CONFIGURATION_GUIDE.md

**路径**: `loadup-components-cache/`

**内容**:

- Binder 配置完整指南
- Redis 和 Caffeine 配置示例
- 混合使用多个 binder 的示例
- 配置优先级说明
- 最佳实践建议

## 配置示例

### Redis Binder 配置

```yaml
# 方式 1: 使用 Spring Boot 默认配置
spring:
  data:
    redis:
      host: localhost
      port: 6379

loadup:
  cache:
    binder: redis

# 方式 2: 覆盖默认配置
spring:
  data:
    redis:
      host: redis-main.example.com  # 主 Redis

loadup:
  cache:
    binder: redis
    binder:
      redis:
        host: redis-cache.example.com  # 缓存专用 Redis（覆盖）
        port: 6380
        password: secret
```

### Caffeine Binder 配置

```yaml
# 方式 1: 使用 Spring Boot 默认配置
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=300s

loadup:
  cache:
    binder: caffeine

# 方式 2: 覆盖默认配置
spring:
  cache:
    caffeine:
      spec: maximumSize=500  # 默认

loadup:
  cache:
    binder: caffeine
    binder:
      caffeine:
        spec: maximumSize=2000,expireAfterWrite=30m  # 覆盖
```

### 混合使用

```yaml
loadup:
  cache:
    binder: redis  # 默认使用 Redis

    # 按缓存名称指定不同的 binder
    binders:
      userCache: redis
      configCache: caffeine

    # 按缓存名称配置
    cache-configs:
      userCache:
        expire-after-write: 30m
      configCache:
        expire-after-write: 10m
        maximum-size: 100

    # Redis 配置
    binder:
      redis:
        host: redis-cache.example.com
        port: 6379

    # Caffeine 配置
    binder:
      caffeine:
        spec: maximumSize=1000,expireAfterWrite=10m
```

## 配置优先级

### Redis

1. `loadup.cache.binder.redis.*` - 自定义配置（最高优先级）
2. `spring.data.redis.*` - Spring Boot 默认配置
3. 内置默认值

### Caffeine

1. `loadup.cache.binder.caffeine.*` - 自定义配置（最高优先级）
2. `spring.cache.caffeine.spec` - Spring Boot 默认配置
3. 内置默认值

## 工作原理

### Redis

1. **Bean 创建时**:
    - `RedisCacheAutoConfiguration` 创建 `RedisBinderCfg` Bean
    - `@ConfigurationProperties(prefix = "loadup.cache.binder.redis")` 绑定配置

2. **连接工厂创建时**:
    - `RedisConnectionFactoryConfiguration` 检查 `loadup.cache.binder.redis.host` 是否存在
    - 如果存在，创建自定义 `RedisConnectionFactory`
    - 配置优先级：自定义配置 > Spring 配置 > 默认值

3. **日志输出**:
   ```
   Creating custom RedisConnectionFactory with loadup.cache.binder.redis configuration
   Redis standalone configuration: host=redis-cache.example.com, port=6380, database=1
   ```

### Caffeine

1. **Bean 创建时**:
    - `CaffeineCacheAutoConfiguration` 创建 `CaffeineBinderCfg` Bean
    - `@ConfigurationProperties(prefix = "loadup.cache.binder.caffeine")` 绑定配置

2. **CacheManager 创建时**:
    - 检查 `caffeineBinderCfg.hasCustomConfig()`
    - 如果有自定义配置，使用 `caffeineBinderCfg.getSpec()`
    - 否则使用 `spring.cache.caffeine.spec`

3. **日志输出**:
   ```
   Applied custom Caffeine cache spec from loadup.cache.binder.caffeine: maximumSize=2000,expireAfterWrite=30m
   ```
   或：
   ```
   Applied default Caffeine cache spec from spring.cache.caffeine: maximumSize=1000,expireAfterWrite=300s
   ```

## 优势

1. **配置清晰**: 不同 binder 的配置完全分离，避免混淆
2. **易于维护**: 每个 binder 的配置独立管理
3. **兼容性好**: 完全兼容 Spring Boot 标准配置
4. **灵活性高**: 可以为不同环境、不同 binder 设置不同的配置

## 迁移指南

如果之前使用了旧的配置路径，需要进行迁移：

### Redis 配置迁移

```yaml
# 旧配置
loadup:
  cache:
    redis:
      host: localhost
      port: 6379

# 新配置
loadup:
  cache:
    binder:
      redis:
        host: localhost
        port: 6379
```

### 注意事项

1. 配置路径改变，但功能保持不变
2. 建议使用新的配置路径，旧路径将来可能废弃
3. 两种配置方式不能混用

## 测试建议

1. **启用 DEBUG 日志**:
   ```yaml
   logging:
     level:
       com.github.loadup.components.cache: DEBUG
   ```

2. **验证配置生效**:
    - 查看日志输出，确认使用的配置来源
    - 使用 Actuator 查看缓存状态
    - 进行功能测试，确保缓存正常工作

3. **测试配置优先级**:
    - 同时配置 Spring Boot 默认配置和自定义配置
    - 验证自定义配置是否覆盖默认配置
    - 检查日志输出的配置信息

## 相关文档

- [Binder 配置指南](BINDER_CONFIGURATION_GUIDE.md)
- [Redis 配置指南](loadup-components-cache-binder-redis/REDIS_CONFIGURATION_GUIDE.md)
- [Caffeine 配置指南](loadup-components-cache-binder-caffeine/CACHE_CONFIGURATION_GUIDE.md)

