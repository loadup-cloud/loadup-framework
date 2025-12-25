# LoadUp Cache 防击穿和雪崩方案总结

## 实现方案

### 1. 配置化策略管理

为每个 cache name 单独配置缓存策略，包括：

```yaml
spring:
  cache:
    redis: # 或 caffeine
      cache-config:
        cache-name:
          # 基础配置
          expire-after-write: 30m           # 写入后过期时间
          expire-after-access: 10m          # 访问后过期时间
          maximum-size: 10000               # 最大缓存条目数

          # 防雪崩配置
          enable-random-expiration: true    # 启用随机过期
          random-offset-seconds: 300        # 随机偏移范围（秒）

          # 防穿透配置
          cache-null-values: true           # 缓存空值
          null-value-expire-after-write: 5m # 空值过期时间

          # 优先级配置
          priority: 5                       # 缓存优先级 (1-10)
          enable-warmup: false              # 启用预热
```

### 2. 核心防护机制

#### 防缓存雪崩 (Cache Avalanche)

- **问题**: 大量缓存同时过期，流量瞬间打到数据库
- **解决方案**:
    - 随机过期偏移：`enable-random-expiration: true`
    - 实际过期时间 = 基础过期时间 + random(0, random-offset-seconds)
    - 不同 cache name 设置不同的基础过期时间

**示例**:

```yaml
users:
  expire-after-write: 30m
  random-offset-seconds: 300    # 实际: 30m + (0~5m)

products:
  expire-after-write: 1h
  random-offset-seconds: 600    # 实际: 1h + (0~10m)
```

#### 防缓存击穿 (Cache Breakdown)

- **问题**: 热点数据过期，瞬间大量请求访问数据库
- **解决方案**:
    - 热点数据设置更长的过期时间
    - 使用 `expire-after-access` 访问延期
    - 设置高优先级，不易被淘汰

**示例**:

```yaml
hot-products:
  expire-after-write: 6h        # 长过期
  expire-after-access: 1h       # 访问后延期
  priority: 9                   # 高优先级
  random-offset-seconds: 3600   # 大随机偏移
```

#### 防缓存穿透 (Cache Penetration)

- **问题**: 查询不存在的数据，绕过缓存直接打数据库
- **解决方案**:
    - 缓存空值：`cache-null-values: true`
    - 空值设置较短过期时间

**示例**:

```yaml
users:
  cache-null-values: true
  null-value-expire-after-write: 5m  # 空值仅缓存5分钟
```

### 3. 实现的核心类

#### CacheExpirationUtil

```java
// 计算带随机偏移的过期时间
Duration calculateExpirationWithRandomOffset(Duration base, LoadUpCacheConfig config)

// 计算按百分比的随机偏移
Duration calculateExpirationWithPercentageOffset(Duration base, double percentage)

// 解析时间字符串 ("30m", "1h", "2d")
Duration parseDuration(String durationStr)
```

#### LoadUpCacheConfig

增强的缓存配置类，支持：

- 基础过期配置（expireAfterWrite, expireAfterAccess）
- 随机过期配置（enableRandomExpiration, randomOffsetSeconds）
- 空值缓存配置（cacheNullValues, nullValueExpireAfterWrite）
- 优先级和预热配置

#### LoadUpCaffeineCacheManager

自定义 Caffeine 缓存管理器，支持：

- 为每个 cache name 注册独立配置
- 动态创建缓存时应用对应策略

### 4. 配置示例

#### 开发环境 (application-dev.yml)

```yaml
loadup:
  cache:
    type: caffeine
    caffeine:
      initial-capacity: 100
      maximum-size: 1000
      expire-after-write-seconds: 300  # 5分钟

spring:
  cache:
    caffeine:
      cache-config:
        test-cache:
          expire-after-write: 5m
          maximum-size: 100
          enable-random-expiration: false  # 开发环境不启用
```

#### 生产环境 (application-prod.yml)

```yaml
loadup:
  cache:
    type: redis
    redis:
      host: redis-cluster.prod
      port: 6379
      database: 0

spring:
  cache:
    redis:
      cache-config:
        # 热点数据
        hot-products:
          expire-after-write: 6h
          expire-after-access: 1h
          maximum-size: 1000
          enable-random-expiration: true
          random-offset-seconds: 3600      # 1小时偏移
          cache-null-values: true
          priority: 9

        # 普通数据
        users:
          expire-after-write: 30m
          maximum-size: 10000
          enable-random-expiration: true
          random-offset-seconds: 300       # 5分钟偏移
          cache-null-values: true
          null-value-expire-after-write: 5m
          priority: 5

        # 临时数据
        temp-data:
          expire-after-write: 5m
          maximum-size: 50000
          enable-random-expiration: true
          random-offset-seconds: 60        # 1分钟偏移
          priority: 2
```

### 5. 使用方式

#### 基本用法

```java

@Service
public class UserService {

    @Resource
    private CacheBinding cacheBinding;

    public User getUser(String userId) {
        // 从缓存获取（自动应用配置的策略）
        User user = cacheBinding.get("users", userId, User.class);

        if (user == null) {
            user = userRepository.findById(userId);
            if (user != null) {
                cacheBinding.set("users", userId, user);
            }
        }

        return user;
    }
}
```

#### 热点数据处理

```java
public Product getHotProduct(String productId) {
    // 使用专门的热点数据缓存（配置了更长过期时间）
    Product product = cacheBinding.get("hot-products", productId, Product.class);

    if (product == null) {
        product = productRepository.findById(productId);
        if (product != null) {
            cacheBinding.set("hot-products", productId, product);
        }
    }

    return product;
}
```

### 6. 优势

1. **配置化管理**: 所有策略通过配置文件管理，无需修改代码
2. **灵活切换**: 不同环境使用不同缓存实现（开发用 Caffeine，生产用 Redis）
3. **自动防护**: 随机过期偏移自动生效，无需手动计算
4. **分级管理**: 热点数据、普通数据、临时数据使用不同策略
5. **易于扩展**: 新增缓存类型只需添加配置，无需修改代码

### 7. 监控建议

建议监控以下指标：

- 各 cache name 的命中率
- 过期时间分布
- 空值缓存命中率
- 缓存大小和内存使用

### 8. 最佳实践

1. **基础过期时间**: 根据数据更新频率设置
    - 热点数据: 6-24 小时
    - 普通数据: 30分钟-2小时
    - 临时数据: 5-10分钟

2. **随机偏移比例**: 建议为基础时间的 10-30%
    - < 10分钟: 10-20%
    - 10-60分钟: 15-25%
    - > 1小时: 20-30%

3. **空值缓存**:
    - 高频查询: 启用空值缓存
    - 空值过期时间: 基础时间的 10-20%
    - 业务敏感数据: 禁用空值缓存

4. **分环境配置**:
    - 开发: Caffeine + 短过期
    - 测试: Redis + 中等过期
    - 生产: Redis + 完整策略

## 总结

该方案通过配置化的方式，为不同的 cache name 提供了灵活的防护策略：

- ✅ **防缓存雪崩**: 随机过期偏移 + 不同基础过期时间
- ✅ **防缓存击穿**: 长过期 + 访问延期 + 高优先级
- ✅ **防缓存穿透**: 空值缓存 + 短过期时间
- ✅ **配置化管理**: 所有策略通过配置文件管理
- ✅ **零代码改动**: 切换策略无需修改业务代码
- ✅ **灵活扩展**: 支持添加新的缓存实现

详细配置请参考 [CACHE_STRATEGY_GUIDE.md](./CACHE_STRATEGY_GUIDE.md)

