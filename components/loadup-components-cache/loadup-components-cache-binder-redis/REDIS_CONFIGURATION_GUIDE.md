# Redis Cache 配置指南

## 概述

LoadUp Framework 的 Redis Cache 组件支持两种配置方式：

1. **默认配置** - 复用 Spring Boot 的 `spring.data.redis` 配置
2. **自定义配置** - 使用 `loadup.cache.redis` 覆盖默认配置

## 配置方式

### 方式 1: 使用 Spring Boot 默认配置（推荐）

默认情况下，Redis Cache 组件会复用 Spring Boot 的 `spring.data.redis` 配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: secret
      database: 0
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms

loadup:
  cache:
    binder: redis
```

这种方式的优点：

- 与 Spring Boot 标准配置保持一致
- 如果应用中已经配置了 Redis，可以直接复用
- 配置简单，无需额外配置

### 方式 2: 使用自定义配置覆盖

如果需要为缓存使用不同的 Redis 服务器或配置，可以通过 `loadup.cache.redis` 覆盖默认配置：

```yaml
# Spring Boot 默认 Redis 配置（用于其他功能）
spring:
  data:
    redis:
      host: redis-main.example.com
      port: 6379

# LoadUp Cache 专用 Redis 配置（覆盖默认）
loadup:
  cache:
    binder: redis
    redis:
      host: redis-cache.example.com  # 覆盖默认 host
      port: 6380                       # 覆盖默认 port
      password: cache-secret           # 覆盖默认 password
      database: 1                      # 覆盖默认 database
```

### 方式 3: 混合配置

可以只覆盖部分配置，未覆盖的配置将使用 Spring Boot 默认值：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: default-secret

loadup:
  cache:
    binder: redis
    redis:
      database: 1  # 只覆盖 database，其他配置使用 spring.data.redis 的默认值
```

## 完整配置选项

### 基础配置

```yaml
loadup:
  cache:
    binder: redis
    redis:
      # 服务器配置
      host: localhost              # Redis 服务器地址
      port: 6379                   # Redis 服务器端口
      database: 0                  # Redis 数据库索引（0-15）
      password: secret             # Redis 密码
      username: default            # Redis 6+ ACL 用户名

      # 超时配置
      connect-timeout: 2000        # 连接超时时间（毫秒）
      read-timeout: 2000           # 读取超时时间（毫秒）

      # SSL 配置
      ssl-enabled: false           # 是否启用 SSL
```

### 连接池配置

```yaml
loadup:
  cache:
    redis:
      # 连接池配置
      max-active: 8                # 最大连接数
      max-idle: 8                  # 最大空闲连接数
      min-idle: 0                  # 最小空闲连接数
      max-wait: -1                 # 最大等待时间（毫秒，-1 表示无限制）
```

### Sentinel 配置

使用 Redis Sentinel 实现高可用：

```yaml
loadup:
  cache:
    binder: redis
    redis:
      pattern: sentinel            # 指定使用 Sentinel 模式
      sentinel-master: mymaster    # Sentinel 主节点名称
      sentinel-nodes: # Sentinel 节点列表
        - sentinel1.example.com:26379
        - sentinel2.example.com:26379
        - sentinel3.example.com:26379
      sentinel-password: sentinel-secret  # Sentinel 密码
      password: redis-secret       # Redis 数据密码
      database: 0
```

### Cluster 配置

使用 Redis Cluster 实现分布式缓存：

```yaml
loadup:
  cache:
    binder: redis
    redis:
      pattern: cluster             # 指定使用 Cluster 模式
      cluster-nodes: # Cluster 节点列表
        - redis1.example.com:7000
        - redis2.example.com:7001
        - redis3.example.com:7002
        - redis4.example.com:7003
        - redis5.example.com:7004
        - redis6.example.com:7005
      max-redirects: 3             # 最大重定向次数
      password: cluster-secret     # Cluster 密码
```

## 配置优先级

配置的优先级顺序（从高到低）：

1. **loadup.cache.redis.\*** - 自定义配置（最高优先级）
2. **spring.data.redis.\*** - Spring Boot 默认配置
3. **内置默认值** - 如果都未配置，使用内置默认值

示例：

```yaml
spring:
  data:
    redis:
      host: localhost     # 优先级 2
      port: 6379          # 优先级 2
      database: 0         # 优先级 2

loadup:
  cache:
    redis:
      host: redis-cache.example.com  # 优先级 1，覆盖 spring.data.redis.host
      database: 1                     # 优先级 1，覆盖 spring.data.redis.database
      # port 未设置，使用 spring.data.redis.port (6379)
```

最终生效配置：

- host: redis-cache.example.com (来自 loadup.cache.redis)
- port: 6379 (来自 spring.data.redis)
- database: 1 (来自 loadup.cache.redis)

## 使用场景

### 场景 1: 单一 Redis 服务器

应用只使用一个 Redis 服务器，所有功能共享：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379

loadup:
  cache:
    binder: redis
    # 不需要额外配置，复用 spring.data.redis
```

### 场景 2: 缓存使用独立 Redis

应用主数据使用一个 Redis，缓存使用另一个 Redis：

```yaml
# 主 Redis（用于其他功能）
spring:
  data:
    redis:
      host: redis-main.example.com
      port: 6379

# 缓存专用 Redis
loadup:
  cache:
    binder: redis
    redis:
      host: redis-cache.example.com
      port: 6380
      database: 0
```

### 场景 3: 不同环境使用不同配置

开发环境使用本地 Redis，生产环境使用远程 Redis：

```yaml
# application-dev.yml
loadup:
  cache:
    binder: redis
    redis:
      host: localhost
      port: 6379

# application-prod.yml
loadup:
  cache:
    binder: redis
    redis:
      host: redis-prod.example.com
      port: 6379
      password: ${REDIS_PASSWORD}
      ssl-enabled: true
```

### 场景 4: 按缓存名称配置不同的过期时间

```yaml
loadup:
  cache:
    binder: redis
    cache-configs:
      userCache:
        expire-after-write: 30m
        cache-null-values: false
      productCache:
        expire-after-write: 1h
        enable-random-expiration: true
        random-offset-seconds: 300
      sessionCache:
        expire-after-write: 15m
```

## 连接模式说明

### Standalone (单机模式)

适用于：

- 开发测试环境
- 小规模应用
- 对高可用性要求不高的场景

配置示例：

```yaml
loadup:
  cache:
    redis:
      host: localhost
      port: 6379
```

### Sentinel (哨兵模式)

适用于：

- 需要自动故障转移
- 中等规模应用
- 对高可用性有要求的场景

优点：

- 自动主从切换
- 监控和通知
- 配置管理

配置示例：

```yaml
loadup:
  cache:
    redis:
      pattern: sentinel
      sentinel-master: mymaster
      sentinel-nodes:
        - sentinel1:26379
        - sentinel2:26379
        - sentinel3:26379
```

### Cluster (集群模式)

适用于：

- 大规模应用
- 需要水平扩展
- 数据量大的场景

优点：

- 数据分片
- 水平扩展
- 高可用性

配置示例：

```yaml
loadup:
  cache:
    redis:
      pattern: cluster
      cluster-nodes:
        - node1:7000
        - node2:7001
        - node3:7002
```

## 常见问题

### Q1: 如何确认使用的是哪个配置？

启用 DEBUG 日志查看配置详情：

```yaml
logging:
  level:
    com.github.loadup.components.cache.redis: DEBUG
```

日志输出示例：

```
Creating custom RedisConnectionFactory with loadup.cache.redis configuration
Redis standalone configuration: host=redis-cache.example.com, port=6380, database=1
```

或：

```
Redis cache binder initialized with Spring Boot default configuration
```

### Q2: 可以同时配置 spring.data.redis 和 loadup.cache.redis 吗？

可以。`loadup.cache.redis` 配置会覆盖 `spring.data.redis` 的对应配置项。

### Q3: 如何测试 Redis 连接？

可以使用 Spring Boot Actuator 的 health endpoint：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  health:
    redis:
      enabled: true
```

访问 `/actuator/health` 查看 Redis 连接状态。

### Q4: 支持哪些 Redis 客户端？

目前支持 Lettuce 客户端（Spring Boot 默认）。Jedis 客户端暂不支持。

## 性能优化建议

1. **连接池配置**
   ```yaml
   loadup:
     cache:
       redis:
         max-active: 20      # 根据并发量调整
         max-idle: 10        # 保持足够的空闲连接
         min-idle: 5         # 预热连接
   ```

2. **超时配置**
   ```yaml
   loadup:
     cache:
       redis:
         connect-timeout: 3000  # 网络不稳定时增加
         read-timeout: 5000     # 根据业务调整
   ```

3. **使用连接池**
    - 避免频繁创建和销毁连接
    - 合理设置池大小

4. **启用随机过期偏移**
    - 防止缓存雪崩
   ```yaml
   loadup:
     cache:
       cache-configs:
         userCache:
           expire-after-write: 30m
           enable-random-expiration: true
           random-offset-seconds: 300
   ```

## 安全建议

1. **使用密码认证**
   ```yaml
   loadup:
     cache:
       redis:
         password: ${REDIS_PASSWORD}  # 使用环境变量
   ```

2. **启用 SSL/TLS**
   ```yaml
   loadup:
     cache:
       redis:
         ssl-enabled: true
   ```

3. **使用 Redis 6+ ACL**
   ```yaml
   loadup:
     cache:
       redis:
         username: cache-user
         password: ${REDIS_PASSWORD}
   ```

4. **网络隔离**
    - 使用内网地址
    - 配置防火墙规则

## 监控和调试

### 启用详细日志

```yaml
logging:
  level:
    com.github.loadup.components.cache.redis: DEBUG
    org.springframework.data.redis: DEBUG
```

### 使用 Spring Boot Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,caches
  metrics:
    enable:
      cache: true
```

访问：

- `/actuator/health` - 查看 Redis 连接状态
- `/actuator/metrics/cache.*` - 查看缓存指标
- `/actuator/caches` - 查看缓存信息

## 相关类

- `RedisBinderCfg` - Redis 配置类
- `RedisConnectionFactoryConfiguration` - Redis 连接工厂配置
- `RedisCacheBinder` - Redis 缓存绑定器
- `RedisCacheAutoConfiguration` - Redis 缓存自动配置

