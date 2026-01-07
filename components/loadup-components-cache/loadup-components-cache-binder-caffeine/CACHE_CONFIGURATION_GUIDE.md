# Caffeine Cache 配置指南

## 概述

LoadUp Framework 的 Caffeine Cache 组件支持两种配置方式：

1. **全局默认配置** - 使用 `spring.cache.caffeine.spec` 为所有缓存设置默认行为
2. **按 Cache 名称自定义配置** - 使用 `loadup.cache.cache-configs` 为特定缓存覆盖默认配置

## 配置方式

### 1. 全局默认配置

使用 Spring Boot 标准的 `spring.cache.caffeine.spec` 属性为所有缓存设置默认配置：

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=300s,expireAfterAccess=60s
```

或使用 properties 格式：

```properties
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=300s,expireAfterAccess=60s
```

**支持的参数：**

- `initialCapacity` - 初始容量
- `maximumSize` - 最大条目数
- `maximumWeight` - 最大权重
- `expireAfterWrite` - 写入后过期时间
- `expireAfterAccess` - 访问后过期时间
- `weakKeys` - 使用弱引用存储键
- `weakValues` - 使用弱引用存储值
- `softValues` - 使用软引用存储值
- `recordStats` - 记录统计信息

### 2. 按 Cache 名称自定义配置

使用 `loadup.cache.cache-configs` 为特定的缓存名称设置自定义配置，这将**覆盖**全局默认配置：

```yaml
loadup:
  cache:
    binder: caffeine
    # 为特定缓存名称设置自定义配置
    cache-configs:
      userCache:
        expire-after-write: 30m
        maximum-size: 10000
        enable-random-expiration: true
        random-offset-seconds: 60
      productCache:
        expire-after-write: 1h
        maximum-size: 5000
      sessionCache:
        expire-after-write: 15m
        expire-after-access: 5m
        maximum-size: 1000

# 未配置的缓存将使用全局默认配置
spring:
  cache:
    caffeine:
      spec: maximumSize=500,expireAfterWrite=10m
```

properties 格式：

```properties
loadup.cache.binder=caffeine
# 自定义配置
loadup.cache.cache-configs.userCache.expire-after-write=30m
loadup.cache.cache-configs.userCache.maximum-size=10000
loadup.cache.cache-configs.userCache.enable-random-expiration=true
loadup.cache.cache-configs.userCache.random-offset-seconds=60
loadup.cache.cache-configs.productCache.expire-after-write=1h
loadup.cache.cache-configs.productCache.maximum-size=5000
# 全局默认配置
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=10m
```

### 3. 按 Cache 名称指定不同的 Binder

如果你的应用同时使用 Caffeine 和 Redis，可以为不同的缓存指定不同的 binder：

```yaml
loadup:
  cache:
    binder: caffeine  # 全局默认使用 caffeine
    binders:
      userCache: redis      # userCache 使用 redis
      productCache: caffeine # productCache 使用 caffeine
    cache-configs:
      userCache:
        expire-after-write: 30m
        maximum-size: 10000
      productCache:
        expire-after-write: 1h
        maximum-size: 5000
```

## 配置优先级

配置的应用顺序（从高到低）：

1. **loadup.cache.cache-configs.[cacheName]** - 特定缓存的自定义配置（最高优先级）
2. **spring.cache.caffeine.spec** - 全局默认配置
3. **内置默认值** - 如果都未配置，使用 Caffeine 的默认值

## 示例场景

### 场景 1: 所有缓存使用相同配置

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m
```

### 场景 2: 大部分缓存使用默认配置，少数缓存特殊处理

```yaml
# 默认配置 - 适用于大多数缓存
spring:
  cache:
    caffeine:
      spec: maximumSize=500,expireAfterWrite=10m

# 特殊配置 - 仅对特定缓存生效
loadup:
  cache:
    cache-configs:
      largeDataCache:
        maximum-size: 10000
        expire-after-write: 1h
      shortLivedCache:
        maximum-size: 100
        expire-after-write: 1m
```

### 场景 3: 防止缓存雪崩

使用随机过期时间偏移来防止大量缓存同时失效：

```yaml
loadup:
  cache:
    cache-configs:
      userCache:
        expire-after-write: 30m
        enable-random-expiration: true
        random-offset-seconds: 300  # 增加 0-5 分钟的随机偏移
```

## 时间单位格式

过期时间支持以下格式：

- `30s` - 30 秒
- `5m` - 5 分钟
- `2h` - 2 小时
- `1d` - 1 天

## 注意事项

1. **动态缓存创建**: 如果代码中使用的缓存名称在配置中未定义，该缓存将使用 `spring.cache.caffeine.spec` 的默认配置
2. **配置生效时机**: 配置在应用启动时加载，运行时修改配置需要重启应用
3. **性能考虑**:
    - `maximumSize` 设置过大可能导致内存溢出
    - `expireAfterWrite` 设置过短可能导致频繁的缓存失效和重建
4. **并发安全**: Caffeine 本身是线程安全的，无需额外的同步控制

## 监控和调试

启用 DEBUG 日志查看缓存配置详情：

```yaml
logging:
  level:
    com.github.loadup.components.cache: DEBUG
```

日志输出示例：

```
Applied default Caffeine cache spec: maximumSize=1000,expireAfterWrite=300s
Configured custom Caffeine cache: name=userCache, maxSize=10000, expireAfterWrite=30m (overrides default spec)
```

## 相关类

- `LoadUpCaffeineCacheManager` - 自定义的 Caffeine 缓存管理器
- `CaffeineCacheAutoConfiguration` - 自动配置类
- `CacheBindingCfg` - 缓存绑定配置类

