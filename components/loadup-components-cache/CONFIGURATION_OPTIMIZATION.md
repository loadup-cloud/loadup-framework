# Cache 组件配置优化总结

## 优化日期

2026-01-06

## 优化目标

移除 `RedisConfig` 和 `CaffeineConfig` 中的冗余基础属性配置，降低学习成本，遵循 Spring Boot 配置最佳实践。

## 主要变更

### 1. CacheProperties 简化

#### 变更前

```java
public static class RedisConfig {
    private String host = "localhost";
    private Integer port = 6379;
    private String password;
    private Integer database = 0;
    private Map<String, LoadUpCacheConfig> cacheConfig;
}

public static class CaffeineConfig {
    private Integer initialCapacity = 1000;
    private Long maximumSize = 5000;
    private Long expireAfterAccessSeconds = 600;
    private Long expireAfterWriteSeconds = 1200;
    private Boolean allowNullValue = false;
    private Map<String, LoadUpCacheConfig> cacheConfig;
}
```

#### 变更后

```java
public static class RedisConfig {
    /** Per-cache configurations for Redis. Use spring.redis.* for connection settings. */
    private Map<String, LoadUpCacheConfig> cacheConfig;
}

public static class CaffeineConfig {
    /** Per-cache configurations for Caffeine. Use spring.cache.caffeine.* for default settings. */
    private Map<String, LoadUpCacheConfig> cacheConfig;
}
```

### 2. 配置方式变更

#### Redis 配置

**变更前：**

```yaml
loadup:
  cache:
    type: redis
    redis:
      host: localhost
      port: 6379
      password: yourpassword
      database: 0
      cache-config:
        users:
          expire-after-write: 30m
```

**变更后：**

```yaml
loadup:
  cache:
    type: redis
    redis:
      cache-config:
        users:
          expire-after-write: 30m

# Redis 连接配置使用 Spring Boot 标准
spring:
  redis:
    host: localhost
    port: 6379
    password: yourpassword
    database: 0
```

#### Caffeine 配置

**变更前：**

```yaml
loadup:
  cache:
    type: caffeine
    caffeine:
      initial-capacity: 1000
      maximum-size: 5000
      expire-after-access-seconds: 600
      expire-after-write-seconds: 1200
      cache-config:
        users:
          expire-after-write: 30m
```

**变更后：**

```yaml
loadup:
  cache:
    type: caffeine
    caffeine:
      cache-config:
        users:
          expire-after-write: 30m

# Caffeine 基础配置使用 Spring Boot 标准
spring:
  cache:
    caffeine:
      spec: initialCapacity=1000,maximumSize=5000,expireAfterWrite=20m
```

### 3. 代码变更

#### RedisCacheAutoConfiguration

- 移除了 `redisConnectionFactory()` bean 方法
- Redis 连接现在通过 Spring Boot 的自动配置提供
- `redisCacheManager` 直接注入 `RedisConnectionFactory`

#### CaffeineCacheAutoConfiguration

- 移除了 `buildDefaultCaffeine()` 方法
- 移除了对 `CaffeineConfig` 基础属性的访问
- Caffeine 基础配置通过 Spring Boot 的 `spring.cache.caffeine.spec` 提供

### 4. 文档更新

- 更新了 `README.md` 配置说明部分
- 更新了 `application.yml.example` 示例配置
- 添加了 Spring Boot 标准配置的参考链接

## 优化效果

### 降低学习成本

- ✅ 用户不需要学习两套配置（LoadUp 自定义 + Spring Boot 标准）
- ✅ 只需要知道 Spring Boot 标准配置即可完成基础设置
- ✅ LoadUp Cache 配置专注于缓存策略（防雪崩、防击穿等）

### 提高可维护性

- ✅ 减少代码重复
- ✅ 遵循 Spring Boot 配置约定
- ✅ 更容易与现有 Spring Boot 项目集成

### 保持功能完整性

- ✅ 所有缓存策略功能保持不变
- ✅ 枚举类型 `CacheType` 提供类型安全
- ✅ IDE 自动提示功能完整（通过 `spring-boot-configuration-processor`）

## 迁移指南

### 如果您之前使用的是旧配置

#### Redis 用户

将原来的：

```yaml
loadup:
  cache:
    redis:
      host: localhost
      port: 6379
```

改为：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

#### Caffeine 用户

将原来的：

```yaml
loadup:
  cache:
    caffeine:
      initial-capacity: 1000
      maximum-size: 5000
```

改为：

```yaml
spring:
  cache:
    caffeine:
      spec: initialCapacity=1000,maximumSize=5000
```

## 兼容性说明

- ⚠️ **不兼容变更**：移除了 `loadup.cache.*` 和 `loadup.cache.*` 的基础连接配置
- ✅ **缓存策略配置保持兼容**：`loadup.cache.cache-config` 和 `loadup.cache.cache-config` 仍然可用
- ✅ **类型枚举向后兼容**：`type: redis` 和 `type: caffeine` 字符串值仍然有效

## 构建验证

```bash
mvn clean install -pl components/loadup-components-cache -am -DskipTests
```

结果：✅ BUILD SUCCESS

## 相关文件

- `CacheProperties.java` - 配置属性类
- `RedisCacheAutoConfiguration.java` - Redis 自动配置
- `CaffeineCacheAutoConfiguration.java` - Caffeine 自动配置
- `README.md` - 使用文档
- `application.yml.example` - 配置示例

