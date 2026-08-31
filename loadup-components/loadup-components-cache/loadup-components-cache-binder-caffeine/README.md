# LoadUp Components Cache Binder - Caffeine

## 📋 概述

基于 Caffeine 的缓存实现，提供高性能的本地缓存支持。

## 🎯 功能特性

- 高性能本地缓存
- 自动过期策略
- LRU/LFU淘汰策略
- 缓存统计功能
- 异步加载支持
- **支持全局默认配置 + 按缓存名称自定义配置**

## 📦 Maven 依赖

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-cache-binder-caffeine</artifactId>
</dependency>
```

## ⚙️ 配置

### 方式 1: 使用 Spring Boot 标准配置（推荐）

使用 `spring.cache.caffeine.spec` 设置全局默认配置，所有未单独配置的缓存都将使用此配置：

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=300s,expireAfterAccess=60s

loadup:
  cache:
    binder: caffeine
```

### 方式 2: 按缓存名称自定义配置（覆盖全局默认）

为特定的缓存名称设置自定义配置，这将**覆盖**全局默认配置：

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=500,expireAfterWrite=10m  # 全局默认

loadup:
  cache:
    binder: caffeine
    cache-configs:
      userCache: # 为 userCache 自定义配置
        expire-after-write: 30m
        maximum-size: 10000
        enable-random-expiration: true
        random-offset-seconds: 60
      productCache: # 为 productCache 自定义配置
        expire-after-write: 1h
        maximum-size: 5000
      # 其他未配置的缓存将使用 spring.cache.caffeine.spec 的默认配置
```

### 方式 3: 旧版配置（兼容）

```yaml
loadup:
  cache:
    type: caffeine
    caffeine:
      max-size: 10000
      expire-after-write: 3600
      initial-capacity: 100
```

### 配置优先级

1. **loadup.cache.cache-configs.[cacheName]** - 特定缓存的自定义配置（最高优先级）
2. **spring.cache.caffeine.spec** - 全局默认配置
3. **内置默认值** - 如果都未配置，使用 Caffeine 的默认值

详细的配置说明请参考：[缓存配置指南](CACHE_CONFIGURATION_GUIDE.md)

## 📝 使用场景

- 单机应用
- 高性能要求的本地缓存
- 不需要分布式共享的缓存
- 需要为不同缓存设置不同过期时间和大小限制的场景

## 💡 最佳实践

1. **使用全局默认配置** - 为大部分缓存设置合理的默认值
2. **按需覆盖** - 仅为有特殊需求的缓存设置自定义配置
3. **防止缓存雪崩** - 对大容量缓存启用随机过期偏移

示例：

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=500,expireAfterWrite=5m

loadup:
  cache:
    cache-configs:
      # 大容量、长期缓存
      userCache:
        maximum-size: 10000
        expire-after-write: 30m
        enable-random-expiration: true
      # 小容量、短期缓存  
      sessionCache:
        maximum-size: 100
        expire-after-write: 1m
```

## 📄 许可证

Apache License 2.0 (Apache-2.0)

详见 [LICENSE](../../../LICENSE) 文件。

---

**最后更新**: 2025-01-07
