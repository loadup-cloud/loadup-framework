# Redis Database 配置测试说明

## 测试文件

### 1. RedisDatabaseConfigUnitTest.java

**类型**: 单元测试（不需要真实 Redis 连接）

**测试内容**:

- ✅ 验证 `RedisBinderCfg` 正确加载自定义配置
- ✅ 验证 Spring Boot 默认配置保持独立
- ✅ 验证配置优先级（自定义配置覆盖默认配置）
- ✅ 验证 database 参数为 Integer 类型
- ✅ 验证 `hasCustomConfig()` 方法
- ✅ 验证所有基础配置字段
- ✅ 验证 database 索引范围（0-15）

**运行方式**:

```bash
mvn test -Dtest=RedisDatabaseConfigUnitTest
```

### 2. RedisDatabaseConfigTest.java

**类型**: 集成测试（需要真实 Redis 连接）

**测试内容**:

- ✅ 验证 `RedisConnectionFactory` 使用自定义 database
- ✅ 验证可以连接到指定的 database 并进行读写操作
- ✅ 验证数据隔离（database 5 的数据不会出现在 database 0）

**前置条件**:

- 需要本地运行 Redis（localhost:6379）
- Redis 需要支持多个 database（默认 0-15）

**运行方式**:

```bash
# 确保 Redis 正在运行
redis-cli ping  # 应该返回 PONG

# 运行测试
mvn test -Dtest=RedisDatabaseConfigTest
```

## 测试配置

### 自定义配置示例

```yaml
loadup:
  cache:
    binder: redis
    binder:
      redis:
        host: redis-cache.example.com
        port: 6380
        database: 5              # 使用 database 5
        password: cache-secret
```

### Spring Boot 默认配置

```yaml
spring:
  data:
    redis:
      host: redis-main.example.com
      port: 6379
      database: 0              # 使用 database 0
      password: main-secret
```

## 配置优先级验证

测试验证了以下优先级：

1. **loadup.cache.binder.redis.database** = 5 （最高优先级）
2. **spring.data.redis.database** = 0 （Spring Boot 默认）

结果：Redis 缓存使用 database 5，而不是 database 0。

## 测试场景

### 场景 1: 缓存使用独立的 database

**需求**: 应用主数据使用 Redis database 0，缓存使用 database 5，实现数据隔离。

**配置**:

```yaml
# 主 Redis（其他功能使用）
spring:
  data:
    redis:
      database: 0

# 缓存专用（覆盖）
loadup:
  cache:
    binder:
      redis:
        database: 5
```

**验证**:

- ✅ 缓存数据写入 database 5
- ✅ database 0 不受影响
- ✅ 数据完全隔离

### 场景 2: 不同环境使用不同 database

**开发环境**:

```yaml
loadup:
  cache:
    binder:
      redis:
        database: 1
```

**测试环境**:

```yaml
loadup:
  cache:
    binder:
      redis:
        database: 2
```

**生产环境**:

```yaml
loadup:
  cache:
    binder:
      redis:
        database: 0
```

## 数据隔离验证

测试 `testDatabaseIsolation()` 验证：

1. **写入 database 5**:
   ```java
   db5Template.opsForValue().set("test:key", "value-in-db5");
   ```

2. **从 database 5 读取**: ✅ 成功，返回 "value-in-db5"

3. **从 database 0 读取**: ✅ 返回 null（数据隔离成功）

## Redis Database 说明

### Redis 默认配置

- 默认支持 16 个 database（索引 0-15）
- 可以通过 `redis.conf` 的 `databases` 参数修改数量
- 不同 database 之间数据完全隔离

### 使用建议

| Database | 推荐用途      |
|----------|-----------|
| 0        | 应用主数据（默认） |
| 1        | 缓存数据      |
| 2        | 会话数据      |
| 3-14     | 其他业务数据    |
| 15       | 测试/临时数据   |

### 注意事项

1. **不推荐在生产环境使用过多 database**
    - 增加管理复杂度
    - 建议使用不同的 Redis 实例替代

2. **Cluster 模式不支持多 database**
    - Redis Cluster 只支持 database 0
    - 需要使用不同的 key 前缀实现隔离

3. **性能考虑**
    - 不同 database 共享 Redis 实例资源
    - 不提供资源隔离，只是逻辑隔离

## 运行所有测试

```bash
# 运行单元测试（不需要 Redis）
mvn test -Dtest=RedisDatabaseConfigUnitTest

# 运行集成测试（需要 Redis）
mvn test -Dtest=RedisDatabaseConfigTest

# 运行所有 Redis 相关测试
mvn test -pl components/loadup-components-cache/loadup-components-cache-binder-redis
```

## 故障排查

### 问题 1: 测试失败 - 无法连接 Redis

**症状**:

```
Connection refused: localhost/127.0.0.1:6379
```

**解决方案**:

```bash
# 启动 Redis
redis-server

# 或使用 Docker
docker run -d -p 6379:6379 redis:latest
```

### 问题 2: database 配置未生效

**检查**:

1. 确认配置路径正确: `loadup.cache.binder.redis.database`
2. 查看日志确认配置加载: `Redis standalone configuration: database=5`
3. 验证 `RedisBinderCfg.getDatabase()` 返回正确值

### 问题 3: 数据隔离测试失败

**原因**: Redis 可能配置了少于 16 个 database

**解决方案**:

```bash
# 检查 Redis 配置
redis-cli CONFIG GET databases

# 如果小于 16，修改配置
redis-cli CONFIG SET databases 16
```

## 相关文档

- [Redis Configuration Guide](../REDIS_CONFIGURATION_GUIDE.md)
- [Binder Configuration Guide](../../BINDER_CONFIGURATION_GUIDE.md)
- [Redis Official Docs - Multiple Databases](https://redis.io/docs/manual/keyspace/#multiple-databases)

