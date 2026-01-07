# LoadUp Cache Binder 配置快速参考

## 🎯 配置路径

| Binder   | 配置路径                             | Spring Boot 默认配置             |
|----------|----------------------------------|------------------------------|
| Redis    | `loadup.cache.binder.redis.*`    | `spring.data.redis.*`        |
| Caffeine | `loadup.cache.binder.caffeine.*` | `spring.cache.caffeine.spec` |

## 📝 基础配置

```yaml
loadup:
  cache:
    binder: redis                    # 全局默认 binder
    binders: # 按缓存名称指定 binder
      userCache: redis
      configCache: caffeine
    cache-configs: # 按缓存名称配置
      userCache:
        expire-after-write: 30m
      configCache:
        expire-after-write: 10m
```

## 🔧 Redis Binder

### 最小配置

```yaml
loadup:
  cache:
    binder: redis
    binder:
      redis:
        host: localhost
        port: 6379
```

### 完整配置

```yaml
loadup:
  cache:
    binder:
      redis:
        # 连接配置
        host: localhost
        port: 6379
        database: 0
        password: secret
        username: default

        # 连接池
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1

        # 超时
        connect-timeout: 2000
        read-timeout: 2000

        # SSL
        ssl-enabled: false
```

### Sentinel 模式

```yaml
loadup:
  cache:
    binder:
      redis:
        pattern: sentinel
        sentinel-master: mymaster
        sentinel-nodes:
          - host1:26379
          - host2:26379
        sentinel-password: secret
```

### Cluster 模式

```yaml
loadup:
  cache:
    binder:
      redis:
        pattern: cluster
        cluster-nodes:
          - host1:7000
          - host2:7001
        max-redirects: 3
```

## ⚡ Caffeine Binder

### 最小配置

```yaml
loadup:
  cache:
    binder: caffeine
    binder:
      caffeine:
        spec: maximumSize=1000,expireAfterWrite=300s
```

### Spec 参数

```yaml
loadup:
  cache:
    binder:
      caffeine:
        spec: >
          initialCapacity=100,
          maximumSize=1000,
          expireAfterWrite=300s,
          expireAfterAccess=60s,
          recordStats
```

**支持的参数**:

- `initialCapacity` - 初始容量
- `maximumSize` - 最大条目数
- `maximumWeight` - 最大权重
- `expireAfterWrite` - 写入后过期
- `expireAfterAccess` - 访问后过期
- `weakKeys` - 弱引用键
- `weakValues` - 弱引用值
- `softValues` - 软引用值
- `recordStats` - 统计信息

## 🎭 混合使用

```yaml
loadup:
  cache:
    binder: redis
    binders:
      userCache: redis       # 分布式缓存
      sessionCache: redis
      configCache: caffeine  # 本地缓存
      dictCache: caffeine

    # Redis 配置
    binder:
      redis:
        host: redis.example.com
        port: 6379

    # Caffeine 配置
    binder:
      caffeine:
        spec: maximumSize=1000,expireAfterWrite=10m
```

## 📊 配置优先级

### Redis

1. `loadup.cache.binder.redis.*` ⭐ (最高)
2. `spring.data.redis.*`
3. 内置默认值

### Caffeine

1. `loadup.cache.binder.caffeine.*` ⭐ (最高)
2. `spring.cache.caffeine.spec`
3. 内置默认值

## 🔍 验证配置

### 启用日志

```yaml
logging:
  level:
    com.github.loadup.components.cache: DEBUG
```

### Redis 日志

```
✅ Creating custom RedisConnectionFactory with loadup.cache.binder.redis configuration
📍 Redis standalone configuration: host=redis.example.com, port=6379, database=0
```

### Caffeine 日志

```
✅ Applied custom Caffeine cache spec from loadup.cache.binder.caffeine: maximumSize=2000
```

## 💡 最佳实践

### 1. 环境分离

```yaml
# application.yml
loadup:
  cache:
    binder: redis

# application-dev.yml
loadup:
  cache:
    binder:
      redis:
        host: localhost

# application-prod.yml
loadup:
  cache:
    binder:
      redis:
        host: redis-prod.example.com
        password: ${REDIS_PASSWORD}
```

### 2. 选择合适的 Binder

| 场景   | 推荐 Binder | 原因       |
|------|-----------|----------|
| 用户会话 | Redis     | 需要分布式共享  |
| 购物车  | Redis     | 需要分布式共享  |
| 配置数据 | Caffeine  | 本地即可，高性能 |
| 字典数据 | Caffeine  | 本地即可，高性能 |
| 分布式锁 | Redis     | 需要分布式协调  |

### 3. 防止缓存雪崩

```yaml
loadup:
  cache:
    cache-configs:
      userCache:
        expire-after-write: 30m
        enable-random-expiration: true
        random-offset-seconds: 300  # 随机偏移 0-5 分钟
```

## 🆘 故障排查

### 问题 1: 配置不生效

**检查**:

- 配置路径是否正确
- 是否启用了对应的 binder
- 查看日志确认配置来源

### 问题 2: Redis 连接失败

**检查**:

- `loadup.cache.binder.redis.host` 是否正确
- 网络连接是否正常
- 密码是否正确
- 使用 Actuator `/actuator/health` 检查

### 问题 3: Caffeine 不生效

**检查**:

- `loadup.cache.binder.caffeine.spec` 格式是否正确
- 查看日志确认 spec 是否被应用
- 检查缓存名称是否正确

## 📚 相关文档

- 📖 [完整配置指南](BINDER_CONFIGURATION_GUIDE.md)
- 🔴 [Redis 详细配置](loadup-components-cache-binder-redis/REDIS_CONFIGURATION_GUIDE.md)
- ☕ [Caffeine 详细配置](loadup-components-cache-binder-caffeine/CACHE_CONFIGURATION_GUIDE.md)
- 📋 [实现总结](BINDER_CONFIGURATION_SEPARATION.md)

