# LoadUp Components Cache Binder - Caffeine

## 📋 概述

基于 Caffeine 的缓存实现，提供高性能的本地缓存支持。

## 🎯 功能特性

- 高性能本地缓存
- 自动过期策略
- LRU/LFU淘汰策略
- 缓存统计功能
- 异步加载支持

## 📦 Maven 依赖

```xml
<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-cache-binder-caffeine</artifactId>
</dependency>
```

## ⚙️ 配置

```yaml
loadup:
  cache:
    type: caffeine
    caffeine:
      max-size: 10000
      expire-after-write: 3600
      initial-capacity: 100
```

## 📝 使用场景

- 单机应用
- 高性能要求的本地缓存
- 不需要分布式共享的缓存

## 📄 许可证

GNU General Public License v3.0 (GPL-3.0)

详见 [LICENSE](../../../LICENSE) 文件。

---

**最后更新**: 2025-12-30
