# LoadUp Cache Component

LoadUp Cache 组件提供了统一的缓存抽象层，支持多种缓存实现的无缝切换。通过简单的配置即可在不同的缓存实现（如 Redis、Caffeine 等）之间切换，无需修改任何业务代码。

## 特性

- **统一接口**：通过 `CacheBinding` 接口提供统一的缓存操作方法（get/set/delete 等）
- **灵活切换**：通过配置文件轻松切换不同的缓存实现，无需修改代码
- **可扩展**：支持自定义缓存实现，只需实现 `CacheBinder` 接口
- **IDE 自动提示**：完善的配置元数据，支持 IDE 配置项自动补全和提示
- **Spring Boot 3 集成**：基于 Spring Boot 3 自动配置机制

## 模块结构

```
loadup-components-cache/
├── loadup-components-cache-api/          # 核心接口定义
├── loadup-components-cache-core/         # 核心配置和默认实现
├── loadup-components-cache-binder-redis/ # Redis 缓存实现
├── loadup-components-cache-binder-caffeine/ # Caffeine 缓存实现
└── loadup-components-cache-test/         # 测试模块
```

## 快速开始

### 1. 使用 Caffeine（本地内存缓存）

#### 添加依赖

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-cache-core</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
<dependency>
<groupId>com.github.loadup.components</groupId>
<artifactId>loadup-components-cache-binder-caffeine</artifactId>
<version>${loadup.framework.version}</version>
</dependency>
```

#### 配置文件（application.yml）

```yaml
loadup:
  cache:
    type: caffeine  # 指定使用 Caffeine
    caffeine:
      initial-capacity: 1000  # 初始容量
      maximum-size: 5000      # 最大缓存条目数
      expire-after-access-seconds: 600   # 访问后过期时间（秒）
      expire-after-write-seconds: 1200   # 写入后过期时间（秒）
```

或使用 properties 格式：

```properties
loadup.cache.type=caffeine
loadup.cache.caffeine.initial-capacity=1000
loadup.cache.caffeine.maximum-size=5000
loadup.cache.caffeine.expire-after-access-seconds=600
loadup.cache.caffeine.expire-after-write-seconds=1200
```

### 2. 使用 Redis（分布式缓存）

#### 添加依赖

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-cache-core</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
<dependency>
<groupId>com.github.loadup.components</groupId>
<artifactId>loadup-components-cache-binder-redis</artifactId>
<version>${loadup.framework.version}</version>
</dependency>
```

#### 配置文件（application.yml）

```yaml
loadup:
  cache:
    type: redis  # 指定使用 Redis
    redis:
      host: localhost    # Redis 服务器地址
      port: 6379        # Redis 端口
      password:         # Redis 密码（可选）
      database: 0       # Redis 数据库索引
```

或使用 properties 格式：

```properties
loadup.cache.type=redis
loadup.cache.redis.host=localhost
loadup.cache.redis.port=6379
loadup.cache.redis.password=
loadup.cache.redis.database=0
```

### 3. 使用代码

```java
import com.github.loadup.components.cache.api.CacheBinding;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class UserService {

    @Resource
    private CacheBinding cacheBinding;

    public void saveUser(String userId, User user) {
        // 保存到缓存
        cacheBinding.set("users", userId, user);
    }

    public User getUser(String userId) {
        // 从缓存获取
        return cacheBinding.get("users", userId, User.class);
    }

    public void deleteUser(String userId) {
        // 删除缓存
        cacheBinding.delete("users", userId);
    }

    public void clearAllUsers() {
        // 清空缓存
        cacheBinding.deleteAll("users");
    }
}
```

## 配置说明

### 通用配置

| 配置项                 | 类型     | 默认值      | 说明                          |
|---------------------|--------|----------|-----------------------------|
| `loadup.cache.type` | String | caffeine | 缓存类型，可选值：`redis`、`caffeine` |

### Caffeine 配置

| 配置项                                                 | 类型      | 默认值  | 说明         |
|-----------------------------------------------------|---------|------|------------|
| `loadup.cache.caffeine.initial-capacity`            | Integer | 1000 | 初始缓存容量     |
| `loadup.cache.caffeine.maximum-size`                | Long    | 5000 | 最大缓存条目数    |
| `loadup.cache.caffeine.expire-after-access-seconds` | Long    | 600  | 访问后过期时间（秒） |
| `loadup.cache.caffeine.expire-after-write-seconds`  | Long    | 1200 | 写入后过期时间（秒） |

### Redis 配置

| 配置项                           | 类型      | 默认值       | 说明           |
|-------------------------------|---------|-----------|--------------|
| `loadup.cache.redis.host`     | String  | localhost | Redis 服务器地址  |
| `loadup.cache.redis.port`     | Integer | 6379      | Redis 端口     |
| `loadup.cache.redis.password` | String  | -         | Redis 密码（可选） |
| `loadup.cache.redis.database` | Integer | 0         | Redis 数据库索引  |

### 按 Cache Name 配置（防击穿和雪崩）

支持为每个 cache name 单独配置策略，有效防止缓存击穿、缓存雪崩和缓存穿透：

| 配置项                             | 类型      | 默认值  | 说明                           |
|---------------------------------|---------|------|------------------------------|
| `expire-after-write`            | String  | -    | 写入后过期时间（如 "30m", "1h", "2d"） |
| `expire-after-access`           | String  | -    | 访问后过期时间                      |
| `maximum-size`                  | Long    | -    | 最大缓存条目数                      |
| `enable-random-expiration`      | Boolean | true | 启用随机过期偏移（防雪崩）                |
| `random-offset-seconds`         | Long    | 60   | 随机偏移范围（秒）                    |
| `cache-null-values`             | Boolean | true | 缓存空值（防穿透）                    |
| `null-value-expire-after-write` | String  | "5m" | 空值过期时间                       |
| `priority`                      | Integer | 5    | 缓存优先级 (1-10)，防止热点数据被淘汰       |

**配置示例**：

```yaml
spring:
  cache:
    redis: # 或 caffeine
      cache-config:
        # 热点数据 - 防击穿
        hot-products:
          expire-after-write: 6h
          expire-after-access: 1h          # 访问后延期
          maximum-size: 1000
          enable-random-expiration: true
          random-offset-seconds: 3600      # 1小时随机偏移
          priority: 9                      # 高优先级，不易淘汰

        # 普通数据 - 防雪崩和穿透
        users:
          expire-after-write: 30m
          maximum-size: 10000
          enable-random-expiration: true
          random-offset-seconds: 300       # 5分钟随机偏移
          cache-null-values: true          # 缓存空值
          null-value-expire-after-write: 5m
          priority: 5
```

**防护机制说明**：

1. **防缓存雪崩**：通过 `enable-random-expiration` 和 `random-offset-seconds` 为每个缓存条目添加随机过期时间偏移，避免大量缓存同时过期
    - 实际过期时间 = 基础过期时间 + random(0, random-offset-seconds)

2. **防缓存击穿**：通过长过期时间 + 访问延期 + 高优先级保护热点数据
    - `expire-after-access`: 每次访问后重置过期时间
    - `priority`: 高优先级数据不易被淘汰

3. **防缓存穿透**：通过 `cache-null-values` 缓存空值，避免恶意查询
    - `null-value-expire-after-write`: 空值使用较短的过期时间

详细配置指南请参考：

- [防击穿和雪崩配置指南](./CACHE_STRATEGY_GUIDE.md)
- [实现方案总结](./IMPLEMENTATION_SUMMARY.md)

## 切换缓存实现

从 Caffeine 切换到 Redis（或反向切换）非常简单：

1. **更新 POM 依赖**：将对应的 binder 依赖替换
2. **修改配置文件**：更新 `loadup.cache.type` 配置项
3. **无需修改代码**：业务代码保持不变

### 示例：从 Caffeine 切换到 Redis

#### 步骤 1：更新依赖

```xml
<!-- 移除或注释 Caffeine 依赖 -->
<!--
<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-cache-binder-caffeine</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
-->

<!-- 添加 Redis 依赖 -->
<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-cache-binder-redis</artifactId>
    <version>${loadup.framework.version}</version>
</dependency>
```

#### 步骤 2：更新配置

```yaml
loadup:
  cache:
    type: redis  # 从 caffeine 改为 redis
    redis:
      host: localhost
      port: 6379
```

#### 步骤 3：重启应用

业务代码无需任何修改，直接重启应用即可。

## 扩展自定义缓存实现

如果需要集成其他缓存实现（如 Memcached、Hazelcast 等），可以按以下步骤进行：

### 1. 创建新的 Binder 模块

```
loadup-components-cache-binder-{cache-name}/
├── pom.xml
└── src/main/java/
    └── com/github/loadup/components/cache/{cache-name}/
        ├── {CacheName}CacheAutoConfiguration.java
        └── impl/{CacheName}CacheBinderImpl.java
```

### 2. 实现 CacheBinder 接口

```java
public class CustomCacheBinderImpl implements CacheBinder {

    @Override
    public String getName() {
        return "CustomCache";
    }

    @Override
    public boolean set(String cacheName, String key, Object value) {
        // 实现 set 逻辑
        return true;
    }

    @Override
    public Object get(String cacheName, String key) {
        // 实现 get 逻辑
        return null;
    }

    @Override
    public <T> T get(String cacheName, String key, Class<T> cls) {
        // 实现 get with type 逻辑
        return null;
    }

    @Override
    public boolean delete(String cacheName, String key) {
        // 实现 delete 逻辑
        return true;
    }

    @Override
    public boolean deleteAll(String cacheName) {
        // 实现 deleteAll 逻辑
        return true;
    }
}
```

### 3. 创建自动配置类

```java

@Configuration
@ConditionalOnProperty(prefix = "loadup.cache", name = "type", havingValue = "custom")
public class CustomCacheAutoConfiguration {

    @Bean
    public CacheBinder cacheBinder() {
        return new CustomCacheBinderImpl();
    }
}
```

### 4. 注册自动配置

在 `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 文件中添加：

```
com.github.loadup.components.cache.custom.CustomCacheAutoConfiguration
```

## API 参考

### CacheBinding 接口

```java
public interface CacheBinding {
    /**
     * 设置缓存
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param value 缓存值
     * @return 是否成功
     */
    boolean set(String cacheName, String key, Object value);

    /**
     * 获取缓存（返回 Object）
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @return 缓存值
     */
    Object get(String cacheName, String key);

    /**
     * 获取缓存（返回指定类型）
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param cls 目标类型
     * @return 缓存值
     */
    <T> T get(String cacheName, String key, Class<T> cls);

    /**
     * 删除缓存
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @return 是否成功
     */
    boolean delete(String cacheName, String key);

    /**
     * 清空指定缓存
     * @param cacheName 缓存名称
     * @return 是否成功
     */
    boolean deleteAll(String cacheName);
}
```

## 最佳实践

### 1. 开发环境使用 Caffeine

在开发环境中使用 Caffeine 可以减少外部依赖，提高开发效率：

```yaml
# application-dev.yml
loadup:
  cache:
    type: caffeine
    caffeine:
      maximum-size: 1000
      expire-after-write-seconds: 300
```

### 2. 生产环境使用 Redis

在生产环境中使用 Redis 以支持分布式缓存：

```yaml
# application-prod.yml
loadup:
  cache:
    type: redis
    redis:
      host: ${REDIS_HOST:redis.example.com}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
      database: 0
```

### 3. 合理设置过期时间

根据数据特性设置合适的过期时间，避免缓存雪崩：

```yaml
loadup:
  cache:
    caffeine:
      expire-after-write-seconds: 1800  # 30分钟
      expire-after-access-seconds: 900  # 15分钟
```

## 技术栈

- **JDK**: 17+
- **Spring Boot**: 3.x
- **Caffeine**: 3.1.8（高性能本地缓存）
- **Redis**: Lettuce 客户端
- **序列化**: Jackson

## 许可证

MIT License - 详见 [LICENSE.txt](LICENSE.txt)

## 联系方式

如有问题或建议，请提交 Issue 或 Pull Request。

