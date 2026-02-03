---
id: components-cache
title: LoadUp Cache Component
---

> 来源: `loadup-components/loadup-components-cache/README.md`

# LoadUp Cache Component

统一的缓存抽象层，支持多种 binder（Redis / Caffeine），并提供丰富的缓存策略（防雪崩、防穿透、优先级、随机过期等）。

主要功能：
- CacheBinding 统一接口（get/set/delete/deleteAll）
- 支持按 cache name 定制策略（expire, priority, null-value caching 等）
- Binder 模式：`loadup-components-cache-binder-caffeine`、`...-redis`
- 提供 CacheExpirationUtil、LoadUpCacheConfig、CacheManagers 等实现

快速配置示例：

```yaml
loadup:
  cache:
    type: redis # 或 caffeine
```

原始模块 README: `loadup-components/loadup-components-cache/README.md`
