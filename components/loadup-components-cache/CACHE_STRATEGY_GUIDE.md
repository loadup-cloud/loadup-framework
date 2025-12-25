# LoadUp Cache 防缓存击穿和雪崩配置指南

## 概述

本文档介绍如何配置 LoadUp Cache 组件以防止缓存击穿、缓存雪崩和缓存穿透等问题。

## 核心防护策略

### 1. 防缓存雪崩 (Cache Avalanche)

**问题**: 大量缓存同时过期，导致大量请求直接打到数据库

**解决方案**: 为不同的缓存设置不同的过期时间，并启用随机过期偏移

```yaml
loadup:
  cache:
    type: redis  # 或 caffeine

loadup:
  cache:
    redis: # 或 caffeine
      cache-config:
        # 用户缓存 - 30分钟 + 随机偏移
        users:
          expire-after-write: 30m
          maximum-size: 10000
          enable-random-expiration: true      # 启用随机过期
          random-offset-seconds: 300          # 随机偏移 0-300秒 (5分钟)

        # 商品缓存 - 1小时 + 随机偏移
        products:
          expire-after-write: 1h
          maximum-size: 5000
          enable-random-expiration: true
          random-offset-seconds: 600          # 随机偏移 0-600秒 (10分钟)

        # 订单缓存 - 2小时 + 随机偏移
        orders:
          expire-after-write: 2h
          maximum-size: 20000
          enable-random-expiration: true
          random-offset-seconds: 1200         # 随机偏移 0-1200秒 (20分钟)
```

**效果**:

- 用户缓存实际过期时间: 30分钟 + (0~5分钟随机)
- 商品缓存实际过期时间: 1小时 + (0~10分钟随机)
- 订单缓存实际过期时间: 2小时 + (0~20分钟随机)

这样即使初始化时间相同，过期时间也会分散开来。

### 2. 防缓存击穿 (Cache Breakdown / Hotspot Invalid)

**问题**: 热点数据过期瞬间，大量请求同时访问数据库

**解决方案**:

1. 热点数据设置更长的过期时间
2. 启用访问后过期策略
3. 提高热点数据的优先级

```yaml
loadup:
  cache:
    redis:
      cache-config:
        # 热点数据缓存
        hot-products:
          expire-after-write: 6h              # 基础过期时间更长
          expire-after-access: 1h             # 访问后1小时内不过期
          maximum-size: 1000
          enable-random-expiration: true
          random-offset-seconds: 3600         # 1小时随机偏移
          priority: 9                         # 高优先级，不易被淘汰

        # 普通数据缓存
        normal-data:
          expire-after-write: 30m
          maximum-size: 10000
          enable-random-expiration: true
          random-offset-seconds: 300
          priority: 5                         # 普通优先级
```

### 3. 防缓存穿透 (Cache Penetration)

**问题**: 查询不存在的数据，缓存和数据库都没有，每次都打到数据库

**解决方案**: 缓存空值，但设置较短的过期时间

```yaml
loadup:
  cache:
    redis:
      cache-config:
        users:
          expire-after-write: 30m
          cache-null-values: true             # 启用空值缓存
          null-value-expire-after-write: 5m   # 空值缓存5分钟
          maximum-size: 10000
```

## 完整配置示例

### Redis 配置

```yaml
# application.yml
loadup:
  cache:
    type: redis
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      database: 0

loadup:
  cache:
    redis:
      cache-config:
        # ==================== 热点数据 ====================
        # 热门商品 - 长缓存 + 大随机偏移
        hot-products:
          expire-after-write: 6h
          expire-after-access: 1h
          maximum-size: 1000
          enable-random-expiration: true
          random-offset-seconds: 3600        # 1小时
          cache-null-values: true
          null-value-expire-after-write: 10m
          priority: 9

        # 热门用户 - 长缓存 + 访问延期
        hot-users:
          expire-after-write: 4h
          expire-after-access: 30m
          maximum-size: 5000
          enable-random-expiration: true
          random-offset-seconds: 1800        # 30分钟
          priority: 8

        # ==================== 普通数据 ====================
        # 用户信息
        users:
          expire-after-write: 30m
          maximum-size: 10000
          enable-random-expiration: true
          random-offset-seconds: 300         # 5分钟
          cache-null-values: true
          null-value-expire-after-write: 5m
          priority: 5

        # 商品信息
        products:
          expire-after-write: 1h
          maximum-size: 5000
          enable-random-expiration: true
          random-offset-seconds: 600         # 10分钟
          cache-null-values: true
          null-value-expire-after-write: 5m
          priority: 5

        # 订单信息
        orders:
          expire-after-write: 2h
          maximum-size: 20000
          enable-random-expiration: true
          random-offset-seconds: 1200        # 20分钟
          cache-null-values: false           # 订单不缓存空值
          priority: 6

        # ==================== 冷数据 ====================
        # 历史数据 - 短缓存
        history-data:
          expire-after-write: 10m
          maximum-size: 50000
          enable-random-expiration: true
          random-offset-seconds: 120         # 2分钟
          cache-null-values: true
          null-value-expire-after-write: 2m
          priority: 3

        # 统计数据 - 短缓存 + 小随机
        statistics:
          expire-after-write: 5m
          maximum-size: 1000
          enable-random-expiration: true
          random-offset-seconds: 60          # 1分钟
          priority: 2

        # ==================== 会话数据 ====================
        # 用户会话
        sessions:
          expire-after-write: 24h
          expire-after-access: 2h
          maximum-size: 100000
          enable-random-expiration: true
          random-offset-seconds: 7200        # 2小时
          cache-null-values: false
          priority: 7
```

### Caffeine 配置

```yaml
# application.yml
loadup:
  cache:
    type: caffeine
    caffeine:
      initial-capacity: 1000
      maximum-size: 10000
      expire-after-access-seconds: 600
      expire-after-write-seconds: 1800

loadup:
  cache:
    caffeine:
      cache-config:
        # 热点数据
        hot-data:
          expire-after-write: 6h
          expire-after-access: 1h
          maximum-size: 1000
          enable-random-expiration: true
          random-offset-seconds: 3600
          priority: 9

        # 普通数据
        normal-data:
          expire-after-write: 30m
          maximum-size: 5000
          enable-random-expiration: true
          random-offset-seconds: 300
          cache-null-values: true
          null-value-expire-after-write: 5m
          priority: 5

        # 临时数据
        temp-data:
          expire-after-write: 5m
          maximum-size: 10000
          enable-random-expiration: true
          random-offset-seconds: 60
          priority: 2
```

## 配置参数说明

| 参数                              | 类型      | 默认值   | 说明                                |
|---------------------------------|---------|-------|-----------------------------------|
| `expire-after-write`            | String  | -     | 写入后过期时间 (e.g., "30m", "1h", "2d") |
| `expire-after-access`           | String  | -     | 访问后过期时间 (e.g., "30m", "1h")       |
| `maximum-size`                  | Long    | -     | 最大缓存条目数                           |
| `enable-random-expiration`      | Boolean | true  | 启用随机过期偏移                          |
| `random-offset-seconds`         | Long    | 60    | 随机偏移范围（秒）                         |
| `cache-null-values`             | Boolean | true  | 是否缓存空值                            |
| `null-value-expire-after-write` | String  | "5m"  | 空值缓存过期时间                          |
| `priority`                      | Integer | 5     | 缓存优先级 (1-10)                      |
| `enable-warmup`                 | Boolean | false | 启用缓存预热                            |

## 最佳实践

### 1. 根据数据特征分类配置

```
热点数据: 长缓存 + 大随机偏移 + 高优先级
普通数据: 中等缓存 + 中等随机偏移 + 普通优先级
冷数据:   短缓存 + 小随机偏移 + 低优先级
临时数据: 极短缓存 + 小随机偏移 + 最低优先级
```

### 2. 随机偏移时间建议

```
基础过期时间 < 10分钟:  随机偏移 10-20%
基础过期时间 10-60分钟: 随机偏移 15-25%
基础过期时间 > 1小时:   随机偏移 20-30%
```

### 3. 空值缓存策略

- 对于经常查询不存在的数据: `cache-null-values: true`
- 空值缓存时间应远小于正常值: `null-value-expire-after-write: 5m`
- 订单等业务敏感数据: `cache-null-values: false`

### 4. 分环境配置

开发环境:

```yaml
# application-dev.yml
loadup:
  cache:
    type: caffeine
    caffeine:
      expire-after-write-seconds: 300  # 5分钟
```

生产环境:

```yaml
# application-prod.yml
loadup:
  cache:
    type: redis
    redis:
      host: redis-cluster.prod
```

## 监控建议

建议监控以下指标:

- 缓存命中率 (Hit Rate)
- 缓存穿透次数
- 缓存雪崩告警
- 各 cache name 的访问频率
- 过期时间分布

## 故障排查

### 问题: 仍然出现缓存雪崩

**检查项**:

1. 确认 `enable-random-expiration: true`
2. 增加 `random-offset-seconds` 值
3. 检查是否有批量初始化缓存的操作

### 问题: 缓存穿透严重

**检查项**:

1. 确认 `cache-null-values: true`
2. 检查 `null-value-expire-after-write` 是否过短
3. 考虑使用布隆过滤器

### 问题: 热点数据频繁失效

**检查项**:

1. 增加 `expire-after-write` 时间
2. 启用 `expire-after-access`
3. 提高 `priority` 值
4. 增加 `maximum-size`

## 示例代码

```java

@Service
public class ProductService {

    @Resource
    private CacheBinding cacheBinding;

    public Product getProduct(String productId) {
        // 1. 尝试从缓存获取
        Product product = cacheBinding.get("products", productId, Product.class);

        if (product != null) {
            return product;
        }

        // 2. 缓存未命中，查询数据库
        product = productRepository.findById(productId);

        // 3. 存入缓存（包括空值，防止缓存穿透）
        if (product != null) {
            cacheBinding.set("products", productId, product);
        } else {
            // 缓存空值（配置中已设置较短过期时间）
            cacheBinding.set("products", productId, NULL_OBJECT);
        }

        return product;
    }

    public Product getHotProduct(String productId) {
        // 热点商品使用专门的 hot-products 缓存
        Product product = cacheBinding.get("hot-products", productId, Product.class);

        if (product != null) {
            return product;
        }

        product = productRepository.findById(productId);

        if (product != null) {
            cacheBinding.set("hot-products", productId, product);
        }

        return product;
    }
}
```

## 总结

通过合理配置不同 cache name 的过期策略，LoadUp Cache 组件可以有效防止:

1. **缓存雪崩**: 通过随机过期偏移，分散缓存过期时间
2. **缓存击穿**: 通过长缓存 + 访问延期 + 高优先级保护热点数据
3. **缓存穿透**: 通过空值缓存防止恶意查询

根据实际业务场景调整配置参数，可以获得最佳的缓存性能和稳定性。

