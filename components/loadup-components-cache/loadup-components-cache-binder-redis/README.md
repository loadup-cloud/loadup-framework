# LoadUp Components Cache Binder - Redis

## 📋 概述

基于 Redis 的缓存实现，提供分布式缓存支持。

## 🎯 功能特性

- 分布式缓存
- 数据持久化
- 主从复制
- 集群支持
- 发布订阅
- 分布式锁

## 📦 Maven 依赖

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-cache-binder-redis</artifactId>
</dependency>
```

## ⚙️ 配置

```yaml
loadup:
  cache:
    type: redis

spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 3000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

## 📝 使用场景

- 分布式应用
- 需要数据共享的场景
- 需要持久化的缓存
- 集群部署

## 📄 许可证

GNU General Public License v3.0 (GPL-3.0)

详见 [LICENSE](../../../LICENSE) 文件。

---

**最后更新**: 2025-12-30
