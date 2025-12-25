# LoadUp Cache 包结构说明

## 模块结构

```
loadup-components-cache/
├── loadup-components-cache-api/              # 核心 API 和配置模块
│   └── src/main/java/com/github/loadup/components/cache/
│       ├── api/                              # 核心接口
│       │   ├── CacheBinder.java             # 缓存绑定器接口
│       │   └── CacheBinding.java            # 缓存绑定接口
│       ├── binding/                          # 接口实现
│       │   └── DefaultCacheBinding.java     # 默认缓存绑定实现
│       ├── cfg/                              # 配置类
│       │   └── LoadUpCacheConfig.java       # 每个cache的配置
│       ├── config/                           # 自动配置
│       │   ├── CacheProperties.java         # 统一配置属性
│       │   └── CacheAutoConfiguration.java  # 自动配置类
│       ├── constans/                         # 常量
│       │   └── CacheConstants.java          # 缓存常量
│       └── util/                             # 工具类
│           └── CacheExpirationUtil.java     # 过期时间计算工具
│
├── loadup-components-cache-binder-caffeine/  # Caffeine 实现
│   └── src/main/java/com/github/loadup/components/cache/caffeine/
│       ├── CaffeineCacheAutoConfiguration.java    # Caffeine 自动配置
│       ├── LoadUpCaffeineCacheManager.java       # Caffeine 缓存管理器
│       └── binder/
│           └── CaffeineCacheBinderImpl.java      # Caffeine 绑定实现
│
├── loadup-components-cache-binder-redis/     # Redis 实现
│   └── src/main/java/com/github/loadup/components/cache/redis/
│       ├── RedisCacheAutoConfiguration.java       # Redis 自动配置
│       ├── LoadUpRedisCacheManager.java          # Redis 缓存管理器
│       └── impl/
│           └── RedisCacheBinderImpl.java         # Redis 绑定实现
│
└── loadup-components-cache-test/            # 测试模块
    └── src/test/java/com/github/loadup/components/cache/
        ├── caffeine/                         # Caffeine 测试
        ├── redis/                            # Redis 测试
        ├── common/                           # 通用测试
        └── integration/                      # 集成测试
```

## 包说明

### api 模块

#### com.github.loadup.components.cache.api

- **CacheBinder**: 缓存绑定器接口，定义基本的 CRUD 操作
- **CacheBinding**: 缓存绑定接口，扩展自 BaseBinding

#### com.github.loadup.components.cache.binding

- **DefaultCacheBinding**: CacheBinding 的默认实现，提供日志和性能监控

#### com.github.loadup.components.cache.cfg

- **LoadUpCacheConfig**: 单个缓存的配置类，包含防雪崩、防穿透等策略

#### com.github.loadup.components.cache.config

- **CacheProperties**: 统一的缓存配置属性类
    - RedisConfig: Redis 配置
    - CaffeineConfig: Caffeine 配置
- **CacheAutoConfiguration**: 自动配置类

#### com.github.loadup.components.cache.util

- **CacheExpirationUtil**: 过期时间计算工具，支持随机偏移

### binder-caffeine 模块

#### com.github.loadup.components.cache.caffeine

- **CaffeineCacheAutoConfiguration**: Caffeine 自动配置
- **LoadUpCaffeineCacheManager**: 扩展的 Caffeine 缓存管理器

#### com.github.loadup.components.cache.caffeine.binder

- **CaffeineCacheBinderImpl**: Caffeine 的 CacheBinder 实现

### binder-redis 模块

#### com.github.loadup.components.cache.redis

- **RedisCacheAutoConfiguration**: Redis 自动配置
- **LoadUpRedisCacheManager**: 扩展的 Redis 缓存管理器

#### com.github.loadup.components.cache.redis.impl

- **RedisCacheBinderImpl**: Redis 的 CacheBinder 实现

### test 模块

#### com.github.loadup.components.cache.caffeine

- Caffeine 缓存的单元测试和集成测试

#### com.github.loadup.components.cache.redis

- Redis 缓存的单元测试和集成测试

#### com.github.loadup.components.cache.common

- 通用测试基类和工具

#### com.github.loadup.components.cache.integration

- 跨缓存类型的集成测试

## 依赖关系

```
api (核心接口和配置)
 ↑
 ├── binder-caffeine (依赖 api)
 └── binder-redis (依赖 api)
```

## 设计原则

1. **接口隔离**: API 模块只包含接口和配置，不包含具体实现
2. **依赖倒置**: Binder 模块依赖 API 接口，而不是具体实现
3. **单一职责**: 每个包负责特定的功能领域
4. **开闭原则**: 通过配置和接口扩展，无需修改核心代码

