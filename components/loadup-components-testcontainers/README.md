# LoadUp Components TestContainers

企业级 TestContainers 基础模块，提供**灵活可切换**的共享容器功能，支持 **7 种容器类型**，可在 TestContainers 和实际服务之间便捷切换，显著提高测试灵活性和执行效率。

## 📦 支持的容器（7种）

| 分类               | 容器              | 默认版本                      | 支持切换 |
|------------------|-----------------|---------------------------|------|
| **📦 Database**  | MySQL           | mysql:8.0                 | ✅    |
|                  | PostgreSQL      | postgres:15-alpine        | ✅    |
|                  | MongoDB         | mongo:7.0                 | ✅    |
| **🔴 Cache**     | Redis           | redis:7-alpine            | ✅    |
| **📨 Messaging** | Kafka           | apache/kafka:4.1.1        | ✅    |
| **🔍 Search**    | Elasticsearch   | elasticsearch:8.11.0      | ✅    |
| **☁️ Cloud**     | LocalStack (S3) | localstack/localstack:3.0 | ✅    |

## ✨ 核心特性

### 🎯 灵活切换（NEW!）

- ✅ **TestContainers 模式**：本地开发，快速隔离，免安装
- ✅ **实际服务模式**：CI 环境，生产环境，性能测试
- ✅ **混合模式**：部分容器，部分实际服务
- ✅ **配置驱动**：通过 Profile 或环境变量控制
- ✅ **零代码修改**：现有测试完全兼容

### 🚀 高性能

- ⚡ **共享容器实例**：单例模式，测试启动速度提升 80-90%
- 🔄 **容器复用**：跨测试类共享，减少资源消耗
- 📊 **CI 优化**：支持使用已有服务，减少启动开销

### 🏗️ 统一架构

- 🎯 **三层架构**：SharedContainer → Initializer → AbstractTest
- 🗂️ **分类组织**：按类型组织（Database、Cache、Messaging、Search、Cloud）
- 📝 **一致性**：所有容器遵循相同模式

## 🚀 快速开始

### 1. 添加依赖

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. 选择模式

#### 模式 A：TestContainers（默认）

```java

@SpringBootTest
@ActiveProfiles("test")  // 使用 application-test.yml
class MyTest extends AbstractMySQLContainerTest {
    @Test
    void test() {
        // 自动使用 TestContainers
    }
}
```

**配置文件** `application-test.yml`：

```yaml
loadup:
  testcontainers:
    enabled: true  # 默认值，可省略
```

#### 模式 B：实际服务

```java

@SpringBootTest
@ActiveProfiles("ci")  // 使用 application-ci.yml
class MyTest extends AbstractMySQLContainerTest {
    @Test
    void test() {
        // 自动使用实际 MySQL 服务
    }
}
```

**配置文件** `application-ci.yml`：

```yaml
loadup:
  testcontainers:
    enabled: false  # 禁用 TestContainers

spring:
  datasource:
    url: jdbc:mysql://mysql-server:3306/testdb
    username: ci_user
    password: ci_password
```

#### 模式 C：混合模式

```yaml
# application-mixed.yml
loadup:
  testcontainers:
    enabled: true
    mysql:
      enabled: false  # 使用实际 MySQL
    redis:
      enabled: true   # 使用 TestContainers Redis

spring:
  datasource:
    url: jdbc:mysql://dev-mysql:3306/devdb
    username: dev
    password: dev
```

## 📚 使用示例

### MySQL

```java

@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryTest extends AbstractMySQLContainerTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveUser() {
        User user = new User("test");
        userRepository.save(user);
        assertNotNull(user.getId());
    }
}
```

### PostgreSQL

```java

@SpringBootTest
class OrderRepositoryTest extends AbstractPostgreSQLContainerTest {
    @Autowired
    private DataSource dataSource;

    @Test
    void testConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertNotNull(conn);
        }
    }
}
```

### MongoDB

```java

@SpringBootTest
class ProductRepositoryTest extends AbstractMongoDBContainerTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void testInsert() {
        Product product = new Product("Test Product");
        mongoTemplate.save(product);
        assertNotNull(product.getId());
    }
}
```

### Redis

```java

@SpringBootTest
class CacheTest extends AbstractRedisContainerTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testCache() {
        redisTemplate.opsForValue().set("key", "value");
        assertEquals("value", redisTemplate.opsForValue().get("key"));
    }
}
```

### Kafka

```java

@SpringBootTest
class MessageTest extends AbstractKafkaContainerTest {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void testSendMessage() {
        kafkaTemplate.send("test-topic", "Hello Kafka");
    }
}
```

### Elasticsearch

```java

@SpringBootTest
class SearchTest extends AbstractElasticsearchContainerTest {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Test
    void testSearch() {
        Product product = new Product("Test");
        restTemplate.save(product);
        // 验证搜索结果
    }
}
```

### LocalStack (S3)

```java

@SpringBootTest
class S3Test extends AbstractLocalStackContainerTest {
    @Autowired
    private S3Client s3Client;

    @Test
    void testUpload() {
        s3Client.createBucket(b -> b.bucket("test"));
        s3Client.putObject(r -> r.bucket("test").key("file.txt"),
                RequestBody.fromString("content"));
    }
}
```

## ⚙️ 配置详解

### 全局配置

```yaml
loadup:
  testcontainers:
    # 全局开关（默认 true）
    enabled: true
```

### 单个容器配置

```yaml
loadup:
  testcontainers:
    mysql:
      enabled: true              # 是否启用（默认 true）
      version: mysql:8.0         # Docker 镜像版本
      database: testdb           # 数据库名
      username: test             # 用户名
      password: test             # 密码

    redis:
      enabled: true
      version: redis:7-alpine

    # ... 其他容器配置
```

### 通过环境变量配置

```bash
# 禁用所有 TestContainers
export LOADUP_TESTCONTAINERS_ENABLED=false

# 禁用特定容器
export LOADUP_TESTCONTAINERS_MYSQL_ENABLED=false

# 运行测试
mvn test
```

### 通过系统属性配置

```bash
mvn test -Dloadup.testcontainers.mysql.enabled=false
```

## 🎯 使用场景

### 场景 1：本地开发

**需求**：快速启动，隔离环境，无需安装服务  
**方案**：使用 TestContainers（默认）  
**配置**：`@ActiveProfiles("test")` 或无需配置

### 场景 2：CI/CD 环境

**需求**：使用已有服务，提高稳定性和速度  
**方案**：禁用 TestContainers，配置实际服务  
**配置**：`@ActiveProfiles("ci")` + 实际服务配置

### 场景 3：性能测试

**需求**：接近生产环境，真实性能数据  
**方案**：使用实际数据库和服务  
**配置**：禁用 TestContainers，连接测试环境

### 场景 4：调试需求

**需求**：查看数据库内容，分析问题  
**方案**：使用实际服务，便于数据查看  
**配置**：临时禁用特定容器

### 场景 5：混合测试

**需求**：部分服务用容器，部分用真实服务  
**方案**：选择性启用/禁用容器  
**配置**：精细化配置每个容器

## 📋 配置示例

### 示例 1：本地开发（默认）

```yaml
# application-test.yml 或 application.yml
# 默认配置，无需任何配置即可使用 TestContainers
```

### 示例 2：CI 环境

```yaml
# application-ci.yml
loadup:
  testcontainers:
    enabled: false

spring:
  datasource:
    url: jdbc:mysql://ci-mysql:3306/testdb
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  redis:
    host: ci-redis
    port: 6379
  kafka:
    bootstrap-servers: ci-kafka:9092
```

### 示例 3：混合模式

```yaml
# application-mixed.yml
loadup:
  testcontainers:
    enabled: true
    # 数据库使用实际服务
    mysql:
      enabled: false
    postgresql:
      enabled: false
    # 其他服务使用 TestContainers
    redis:
      enabled: true
    mongodb:
      enabled: true
    kafka:
      enabled: true

spring:
  datasource:
    url: jdbc:mysql://dev-mysql:3306/devdb
    username: dev
    password: dev
```

### 示例 4：自定义容器版本

```yaml
loadup:
  testcontainers:
    mysql:
      enabled: true
      version: mysql:8.0.32  # 使用特定版本
    redis:
      enabled: true
      version: redis:7.2-alpine
```

## 🏗️ 架构设计

### 三层架构

```
┌─────────────────────────────────────────┐
│      AbstractTest (测试基类)            │
│   - 声明式配置                          │
│   - 继承即用                            │
└──────────────┬──────────────────────────┘
               │ @ContextConfiguration
               ↓
┌─────────────────────────────────────────┐
│      Initializer (初始化器)             │
│   - 条件判断                            │
│   - 属性注入                            │
│   - Spring 集成                         │
└──────────────┬──────────────────────────┘
               │ 读取配置
               ↓
┌─────────────────────────────────────────┐
│   SharedContainer (共享容器)             │
│   - 条件启动                            │
│   - 单例模式                            │
│   - 生命周期管理                        │
└─────────────────────────────────────────┘
```

### 决策流程

```
测试启动
   ↓
读取配置 (loadup.testcontainers.{type}.enabled)
   ↓
   ├─ true  → 启动 TestContainer → 注入容器属性
   └─ false → 跳过容器启动 → 使用配置文件中的实际服务配置
```

## 🔄 迁移指南

### 从旧版本迁移

**好消息**：无需任何代码修改！✅

现有测试代码**完全兼容**：

```java
// 旧代码 - 无需修改
@SpringBootTest
class MyTest extends AbstractMySQLContainerTest {
    @Test
    void test() {
        // 仍然正常工作
    }
}
```

**新增能力**：通过配置控制行为

```yaml
# 新增配置（可选）
loadup:
  testcontainers:
    mysql:
      enabled: false  # 切换到实际服务
```

## 📊 性能对比

| 场景        | TestContainers | 实际服务  | 说明          |
|-----------|----------------|-------|-------------|
| **本地开发**  | ⭐⭐⭐⭐⭐          | ⭐⭐    | 容器快速启动，无需安装 |
| **CI 环境** | ⭐⭐⭐            | ⭐⭐⭐⭐⭐ | 实际服务更稳定快速   |
| **性能测试**  | ⭐⭐             | ⭐⭐⭐⭐⭐ | 实际服务反映真实性能  |
| **隔离性**   | ⭐⭐⭐⭐⭐          | ⭐⭐⭐   | 容器完全隔离      |
| **调试便利**  | ⭐⭐⭐            | ⭐⭐⭐⭐⭐ | 实际服务便于数据查看  |

## 🛠️ 高级用法

### 编程式控制

```java
// 检查容器是否启用
if(SharedMySQLContainer.isEnabled()){
// 使用容器
String url = SharedMySQLContainer.getJdbcUrl();
}else{
        // 使用实际服务
        }
```

### 动态切换

```java

@SpringBootTest
@TestPropertySource(properties = {
        "loadup.testcontainers.mysql.enabled=false"
})
class MyTest {
    // 仅在此测试中禁用 MySQL 容器
}
```

### 自定义初始化器

```java
public class CustomInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (SharedMySQLContainer.isEnabled()) {
            // 容器模式的额外配置
        } else {
            // 实际服务模式的额外配置
        }
    }
}
```

## ❓ 常见问题

### Q1: 如何在 CI 环境中禁用所有容器？

```yaml
# application-ci.yml
loadup:
  testcontainers:
    enabled: false  # 全局禁用
```

### Q2: 如何只禁用某个特定容器？

```yaml
loadup:
  testcontainers:
    enabled: true    # 全局启用
    mysql:
      enabled: false # 只禁用 MySQL
```

### Q3: 容器版本如何自定义？

```yaml
loadup:
  testcontainers:
    mysql:
      version: mysql:8.0.32  # 指定版本
```

### Q4: 如何在测试中知道是否使用了容器？

```java
boolean usingContainer = SharedMySQLContainer.isEnabled();
```

### Q5: 现有测试需要修改吗？

不需要！完全向后兼容，默认行为不变。

## 📈 最佳实践

1. **本地开发**：使用 TestContainers（默认）
2. **CI 环境**：使用实际服务（配置 `enabled: false`）
3. **性能测试**：使用实际服务，接近生产环境
4. **调试问题**：临时切换到实际服务，便于数据查看
5. **版本管理**：通过配置文件统一管理容器版本
6. **Profile 隔离**：不同环境使用不同 Profile

## 📝 更新日志

### v2.0.0 (2026-01-08)

- ✨ **新增**：支持 TestContainers 和实际服务灵活切换
- ✨ **新增**：配置驱动的容器启用/禁用机制
- ✨ **新增**：混合模式支持（部分容器，部分实际服务）
- ✨ **新增**：通过 Profile 控制不同环境
- ✅ **兼容**：现有代码无需修改，完全向后兼容

### v1.0.0

- 初始版本，支持 7 种容器
- 共享容器机制
- 三层架构设计

## 📄 许可证

Copyright (c) 2026 LoadUp Framework  
Licensed under the Apache License 2.0

---

**💡 提示**：更多详细信息请参考 [ARCHITECTURE.md](ARCHITECTURE.md)

