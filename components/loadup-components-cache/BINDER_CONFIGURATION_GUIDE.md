# LoadUp Cache Binder 配置指南

## 概述

LoadUp Cache 支持灵活的 binder 配置方式，可以为不同的 binder 类型（Redis、Caffeine）设置独立的配置。

## 配置结构

```yaml
loadup:
  cache:
    # 全局默认 binder
    binder: redis

    # 按缓存名称指定 binder
    binders:
      userCache: redis
      productCache: caffeine

    # 按缓存名称配置通用属性
    cache-configs:
      userCache:
        expire-after-write: 30m
        maximum-size: 10000
      productCache:
        expire-after-write: 1h
        maximum-size: 5000

    # Redis Binder 专属配置
    binder:
      redis:
        host: redis-cache.example.com
        port: 6380
        password: secret
        database: 1

    # Caffeine Binder 专属配置
    binder:
      caffeine:
        spec: maximumSize=2000,expireAfterWrite=30m
```

## Redis Binder 配置

### 配置路径：`loadup.cache.binder.redis.*`

### 配置优先级

1. **loadup.cache.binder.redis.\*** - 自定义配置（最高优先级）
2. **spring.data.redis.\*** - Spring Boot 默认配置
3. **内置默认值** - 兜底配置

### 配置示例

#### 示例 1: 使用 Spring Boot 默认配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: secret

loadup:
  cache:
    binder: redis
    # 不需要额外配置，自动复用 spring.data.redis
```

#### 示例 2: 覆盖默认配置

```yaml
# Spring Boot 默认配置（用于其他功能）
spring:
  data:
    redis:
      host: redis-main.example.com
      port: 6379

# LoadUp Cache 专用配置（覆盖默认）
loadup:
  cache:
    binder: redis
    binder:
      redis:
        host: redis-cache.example.com  # 覆盖
        port: 6380                       # 覆盖
        password: cache-secret           # 覆盖
        database: 1                      # 覆盖
```

#### 示例 3: Sentinel 配置

```yaml
loadup:
  cache:
    binder: redis
    binder:
      redis:
        pattern: sentinel
        sentinel-master: mymaster
        sentinel-nodes:
          - sentinel1.example.com:26379
          - sentinel2.example.com:26379
          - sentinel3.example.com:26379
        sentinel-password: sentinel-secret
        password: redis-secret
```

#### 示例 4: Cluster 配置

```yaml
loadup:
  cache:
    binder: redis
    binder:
      redis:
        pattern: cluster
        cluster-nodes:
          - redis1.example.com:7000
          - redis2.example.com:7001
          - redis3.example.com:7002
        max-redirects: 3
        password: cluster-secret
```

### 完整配置选项

```yaml
loadup:
  cache:
    binder:
      redis:
        # 基础配置
        host: localhost              # Redis 服务器地址
        port: 6379                   # Redis 服务器端口
        database: 0                  # Redis 数据库索引
        password: secret             # Redis 密码
        username: default            # Redis 6+ ACL 用户名

        # 超时配置
        connect-timeout: 2000        # 连接超时（毫秒）
        read-timeout: 2000           # 读取超时（毫秒）

        # SSL 配置
        ssl-enabled: false           # 是否启用 SSL

        # 连接池配置
        max-active: 8                # 最大连接数
        max-idle: 8                  # 最大空闲连接数
        min-idle: 0                  # 最小空闲连接数
        max-wait: -1                 # 最大等待时间（毫秒）

        # Sentinel 配置
        pattern: sentinel            # 部署模式
        sentinel-master: mymaster    # Sentinel 主节点名称
        sentinel-nodes: # Sentinel 节点列表
          - host1:26379
          - host2:26379
        sentinel-password: secret    # Sentinel 密码

        # Cluster 配置
        pattern: cluster             # 部署模式
        cluster-nodes: # Cluster 节点列表
          - host1:7000
          - host2:7001
        max-redirects: 3             # 最大重定向次数
```

## Caffeine Binder 配置

### 配置路径：`loadup.cache.binder.caffeine.*`

### 配置优先级

1. **loadup.cache.binder.caffeine.\*** - 自定义配置（最高优先级）
2. **spring.cache.caffeine.spec** - Spring Boot 默认配置
3. **内置默认值** - 兜底配置

### 配置示例

#### 示例 1: 使用 Spring Boot 默认配置

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=300s

loadup:
  cache:
    binder: caffeine
    # 不需要额外配置，自动复用 spring.cache.caffeine.spec
```

#### 示例 2: 覆盖默认配置

```yaml
# Spring Boot 默认配置（用于其他功能）
spring:
  cache:
    caffeine:
      spec: maximumSize=500,expireAfterWrite=10m

# LoadUp Cache 专用配置（覆盖默认）
loadup:
  cache:
    binder: caffeine
    binder:
      caffeine:
        spec: maximumSize=2000,expireAfterWrite=30m  # 覆盖
```

### Caffeine Spec 参数

```yaml
loadup:
  cache:
    binder:
      caffeine:
        # spec 格式：key1=value1,key2=value2,...
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
- `expireAfterWrite` - 写入后过期时间
- `expireAfterAccess` - 访问后过期时间
- `weakKeys` - 使用弱引用存储键
- `weakValues` - 使用弱引用存储值
- `softValues` - 使用软引用存储值
- `recordStats` - 记录统计信息

## 混合使用 Redis 和 Caffeine

可以在同一个应用中混合使用 Redis 和 Caffeine：

```yaml
loadup:
  cache:
    binder: redis  # 全局默认使用 Redis

    # 按缓存名称指定不同的 binder
    binders:
      userCache: redis      # 用户缓存使用 Redis（分布式）
      sessionCache: redis   # 会话缓存使用 Redis（分布式）
      productCache: caffeine # 产品缓存使用 Caffeine（本地）
      configCache: caffeine  # 配置缓存使用 Caffeine（本地）

    # 按缓存名称配置过期时间和大小
    cache-configs:
      userCache:
        expire-after-write: 30m
        maximum-size: 10000
      sessionCache:
        expire-after-write: 15m
      productCache:
        expire-after-write: 1h
        maximum-size: 5000
      configCache:
        expire-after-write: 10m
        maximum-size: 100

    # Redis 配置
    binder:
      redis:
        host: redis-cache.example.com
        port: 6379
        password: secret

    # Caffeine 配置
    binder:
      caffeine:
        spec: maximumSize=1000,expireAfterWrite=10m
```

## 配置验证

### 查看生效的配置

启用 DEBUG 日志：

```yaml
logging:
  level:
    com.github.loadup.components.cache: DEBUG
```

**Redis 日志输出**:

```
Creating custom RedisConnectionFactory with loadup.cache.binder.redis configuration
Redis standalone configuration: host=redis-cache.example.com, port=6380, database=1
```

或：

```
Redis cache binder initialized with Spring Boot default configuration
```

**Caffeine 日志输出**:

```
Applied custom Caffeine cache spec from loadup.cache.binder.caffeine: maximumSize=2000,expireAfterWrite=30m
```

或：

```
Applied default Caffeine cache spec from spring.cache.caffeine: maximumSize=1000,expireAfterWrite=300s
```

### 使用 Actuator 监控

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,caches
```

访问：

- `/actuator/health` - 查看 Redis 连接状态
- `/actuator/caches` - 查看缓存信息
- `/actuator/metrics/cache.*` - 查看缓存指标

## 最佳实践

### 1. 配置分离

将 binder 配置与业务配置分开：

```yaml
# application.yml - 通用配置
loadup:
  cache:
    binder: redis
    cache-configs:
      userCache:
        expire-after-write: 30m

# application-dev.yml - 开发环境
loadup:
  cache:
    binder:
      redis:
        host: localhost

# application-prod.yml - 生产环境
loadup:
  cache:
    binder:
      redis:
        host: redis-prod.example.com
        password: ${REDIS_PASSWORD}
        ssl-enabled: true
```

### 2. 使用环境变量

敏感信息使用环境变量：

```yaml
loadup:
  cache:
    binder:
      redis:
        password: ${REDIS_PASSWORD}
        username: ${REDIS_USERNAME:default}
```

### 3. 合理选择 Binder

- **使用 Redis**:
    - 需要分布式共享的数据（用户会话、购物车等）
    - 需要数据持久化
    - 多实例部署

- **使用 Caffeine**:
    - 纯本地数据（配置、字典等）
    - 高性能要求
    - 单实例或每个实例独立缓存

### 4. 配置优化

```yaml
loadup:
  cache:
    # 大容量、长期缓存使用 Redis
    binders:
      userCache: redis
      orderCache: redis

      # 小容量、短期缓存使用 Caffeine
      configCache: caffeine
      dictCache: caffeine

    cache-configs:
      userCache:
        expire-after-write: 30m
        enable-random-expiration: true  # 防止缓存雪崩
      configCache:
        expire-after-write: 5m
        maximum-size: 100
```

## 相关文档

- [Redis 配置指南](loadup-components-cache-binder-redis/REDIS_CONFIGURATION_GUIDE.md)
- [Caffeine 配置指南](loadup-components-cache-binder-caffeine/CACHE_CONFIGURATION_GUIDE.md)

