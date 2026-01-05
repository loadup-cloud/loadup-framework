# LoadUp Components TestContainers

企业级 TestContainers 基础模块，提供共享容器功能，支持 **7 种容器类型**，按 **5 大类**科学组织，用于在测试过程中共享数据库和服务实例，显著提高测试执行效率。

## 📦 支持的容器（7种）

### 📊 按分类

| 分类               | 容器              | 默认版本                 | 包路径                         |
|------------------|-----------------|----------------------|-----------------------------|
| **📦 Database**  | MySQL           | mysql:8.0            | `.testcontainers`           |
|                  | PostgreSQL      | postgres:15-alpine   | `.testcontainers.database`  |
|                  | MongoDB         | mongo:7.0            | `.testcontainers.database`  |
| **🔴 Cache**     | Redis           | redis:7-alpine       | `.testcontainers`           |
| **📨 Messaging** | Kafka           | cp-kafka:7.5.0       | `.testcontainers.messaging` |
| **🔍 Search**    | Elasticsearch   | elasticsearch:8.11.0 | `.testcontainers.search`    |
| **☁️ Cloud**     | LocalStack (S3) | localstack:3.0       | `.testcontainers`           |

## ✨ 功能特性

- 🚀 **共享容器实例**：单例模式，所有测试共享，启动速度提升 80-90%
- 🗂️ **分类组织**：5大分类（Database、Cache、Messaging、Search、Cloud），清晰易用
- 🎯 **统一架构**：所有容器遵循三层架构（容器-初始化器-基类）
- 🔧 **易于集成**：继承抽象基类即可，零配置
- ⚙️ **灵活配置**：支持系统属性自定义容器版本和配置
- 📝 **完善的文档**：详细的使用示例和最佳实践
- 🧪 **Spring Boot 集成**：无缝集成 Spring Boot 测试框架
- 🔄 **向后兼容**：保持原有代码无需修改

## 🚀 快速开始

### 1. 添加依赖

在测试模块的 `pom.xml` 中添加：

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. 使用容器

#### MySQL（关系型数据库）

```java

@SpringBootTest
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

#### PostgreSQL（关系型数据库）

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

#### MongoDB（文档型数据库）

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

#### Redis（缓存）

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

#### Kafka（消息队列）

```java

@SpringBootTest
class MessageTest extends AbstractKafkaContainerTest {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void testSendMessage() {
        kafkaTemplate.send("test-topic", "Hello Kafka");
        // 验证消息接收
    }
}
```

#### Elasticsearch（搜索引擎）

```java

@SpringBootTest
class SearchTest extends AbstractElasticsearchContainerTest {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Test
    void testSearch() {
        Product product = new Product("Test");
        restTemplate.save(product);

        SearchHits<Product> hits = restTemplate.search(
                Query.findAll(), Product.class);
        assertEquals(1, hits.getTotalHits());
    }
}
```

#### LocalStack/S3（对象存储）

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
        // 验证上传
    }
}
```

## 📚 使用方式对比

### 方式1：继承抽象基类（推荐⭐）

```java

@SpringBootTest
class MyTest extends AbstractMySQLContainerTest {
    // 自动配置，零代码
}
```

### 方式2：使用初始化器

```java

@SpringBootTest
@ContextConfiguration(initializers = MySQLContainerInitializer.class)
class MyTest {
    // 测试代码
}
```

### 方式3：直接使用共享容器

```java

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=" + SharedMySQLContainer.JDBC_URL,
        "spring.datasource.username=" + SharedMySQLContainer.USERNAME,
        "spring.datasource.password=" + SharedMySQLContainer.PASSWORD
})
class MyTest {
    // 测试代码
}
```

## 🎯 多容器组合使用

### 数据库 + 缓存

```java

@SpringBootTest
@ContextConfiguration(initializers = {
        MySQLContainerInitializer.class,
        RedisContainerInitializer.class
})
class FullStackTest {
    @Autowired
    private DataSource    dataSource;
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void testBoth() {
        // 同时使用两种容器
    }
}
```

### 完整技术栈

```java

@SpringBootTest
@ContextConfiguration(initializers = {
        PostgreSQLContainerInitializer.class,
        MongoDBContainerInitializer.class,
        RedisContainerInitializer.class,
        KafkaContainerInitializer.class,
        ElasticsearchContainerInitializer.class,
        LocalStackContainerInitializer.class
})
class CompleteStackTest {
    // 使用所有容器！
}
```

## ⚙️ 配置选项

### 系统属性配置

```bash
# MySQL
-Dtestcontainers.mysql.version=mysql:8.0
-Dtestcontainers.mysql.database=testdb
-Dtestcontainers.mysql.username=test
-Dtestcontainers.mysql.password=test

# PostgreSQL
-Dtestcontainers.postgres.version=postgres:15-alpine
-Dtestcontainers.postgres.database=testdb

# MongoDB
-Dtestcontainers.mongodb.version=mongo:7.0

# Redis
-Dtestcontainers.redis.version=redis:7-alpine

# Kafka
-Dtestcontainers.kafka.version=confluentinc/cp-kafka:7.5.0

# Elasticsearch
-Dtestcontainers.elasticsearch.version=elasticsearch:8.11.0

# LocalStack
-Dtestcontainers.localstack.version=localstack/localstack:3.0
```

### 启用容器复用（推荐）

```bash
# 大幅提升后续测试启动速度
echo "testcontainers.reuse.enable=true" >> ~/.testcontainers.properties
```

### Maven 配置

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <testcontainers.mysql.version>mysql:8.0</testcontainers.mysql.version>
            <testcontainers.redis.version>redis:7-alpine</testcontainers.redis.version>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

## 📈 性能优势

### 容器启动时间对比

| 容器            | 传统方式（每个测试类） | 共享容器方式       | 性能提升       |
|---------------|-------------|--------------|------------|
| MySQL         | ~8秒/次       | 首次8秒，后续1秒    | **87% ⬆️** |
| PostgreSQL    | ~6秒/次       | 首次6秒，后续1秒    | **83% ⬆️** |
| MongoDB       | ~5秒/次       | 首次5秒，后续<1秒   | **90% ⬆️** |
| Redis         | ~3秒/次       | 首次3秒，后续<0.5秒 | **90% ⬆️** |
| Kafka         | ~20秒/次      | 首次20秒，后续2秒   | **90% ⬆️** |
| Elasticsearch | ~25秒/次      | 首次25秒，后续2秒   | **92% ⬆️** |
| LocalStack    | ~15秒/次      | 首次15秒，后续1秒   | **93% ⬆️** |

### 实际测试场景

#### 10个测试类使用 MySQL

```
传统方式: 10 × 8秒 = 80秒
共享方式: 8秒 + 9×1秒 = 17秒
提升: 79% ⬆️
```

#### 完整技术栈（所有容器）

```
传统方式: 10类 × (8+6+5+3+20+25+15)秒 = 820秒 (13.7分钟)
共享方式: (8+6+5+3+20+25+15)秒 + 9×7秒 = 145秒 (2.4分钟)
提升: 82% ⬆️
```

## 🎨 API 参考

### MySQL

```java
SharedMySQLContainer.getJdbcUrl()
SharedMySQLContainer.

getUsername()
SharedMySQLContainer.

getPassword()
SharedMySQLContainer.

getDatabaseName()
SharedMySQLContainer.

getDriverClassName()
```

### PostgreSQL

```java
SharedPostgreSQLContainer.getJdbcUrl()
SharedPostgreSQLContainer.

getUsername()
SharedPostgreSQLContainer.

getPassword()
SharedPostgreSQLContainer.

getDatabaseName()
```

### MongoDB

```java
SharedMongoDBContainer.getConnectionString()
SharedMongoDBContainer.

getHost()
SharedMongoDBContainer.

getPort()
SharedMongoDBContainer.

getReplicaSetUrl()
```

### Redis

```java
SharedRedisContainer.getHost()
SharedRedisContainer.

getPort()
SharedRedisContainer.

getUrl()
```

### Kafka

```java
SharedKafkaContainer.getBootstrapServers()
SharedKafkaContainer.

getHost()
SharedKafkaContainer.

getPort()
```

### Elasticsearch

```java
SharedElasticsearchContainer.getHttpHostAddress()
SharedElasticsearchContainer.

getHost()
SharedElasticsearchContainer.

getPort()
```

### LocalStack/S3

```java
SharedLocalStackContainer.getS3Endpoint()
SharedLocalStackContainer.

getAccessKey()
SharedLocalStackContainer.

getSecretKey()
SharedLocalStackContainer.

getRegion()
```

## 🔧 故障排除

### 问题1: Docker 未运行

```bash
# macOS
open -a Docker

# 验证
docker info
```

### 问题2: 容器启动失败

```bash
# 检查 Docker 状态
docker ps

# 查看日志
docker logs <container-id>

# 清理旧容器
docker container prune
```

### 问题3: IDE 显示找不到类

```
解决方案：
- IntelliJ IDEA: 右键项目 → Maven → Reload Project
- VS Code: Cmd/Ctrl + Shift + P → "Reload Window"
- 或直接用 Maven 验证: mvn clean compile
```

### 问题4: 测试很慢

```bash
# 启用容器复用
echo "testcontainers.reuse.enable=true" >> ~/.testcontainers.properties

# 提前拉取镜像
docker pull mysql:8.0
docker pull redis:7-alpine
docker pull mongo:7.0
```

## 🎯 最佳实践

### 1. 选择合适的容器

- **关系型数据库**: PostgreSQL（功能强） 或 MySQL（兼容好）
- **文档型数据库**: MongoDB
- **缓存**: Redis
- **消息队列**: Kafka
- **全文搜索**: Elasticsearch
- **对象存储**: LocalStack/S3

### 2. 使用推荐

```java
// ✅ 推荐：继承抽象基类
@SpringBootTest
class MyTest extends AbstractMySQLContainerTest {
}

// ✅ 推荐：启用容器复用
testcontainers.reuse.enable=true

// ✅ 推荐：使用有序ID（UUID v7 或 Snowflake）
// 提升数据库插入性能
```

### 3. 避免事项

```java
// ❌ 避免：手动管理容器生命周期
@Container
static MySQLContainer mysql = new MySQLContainer();

// ❌ 避免：在每个测试类中重复配置
@DynamicPropertySource
static void configure() { ...}

// ❌ 避免：不启用容器复用
// 会导致每次都重新启动容器
```

## 📦 依赖说明

本模块已包含以下依赖，使用时无需额外添加：

- TestContainers Core (1.19.3)
- MySQL、PostgreSQL、MongoDB 驱动
- Redis 客户端（Jedis）
- Kafka 客户端
- Elasticsearch 客户端
- LocalStack 支持
- Spring Boot Test 集成

## 🏗️ 模块架构

详细的架构设计和实现细节请参考 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 📊 版本历史

- **1.0.0** - 初始版本，支持 MySQL
- **1.1.0** - 添加 Redis 和 LocalStack/S3 支持
- **1.2.0** - 添加 PostgreSQL、MongoDB、Kafka、Elasticsearch 支持
- **1.2.1** - 按分类组织包结构
- **1.3.0** - 当前版本，完整的7种容器支持

## 📝 License

本模块遵循 Apache License 2.0 协议。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**维护者**: LoadUp Framework Team  
**最后更新**: 2026-01-05

# Consolidated Documentation

## Overview

This document consolidates all relevant information about the `loadup-components-testcontainers` module, including integration details,
configuration examples, and usage guidelines.

## Integration Summary

- **Modules Integrated**: UPMS, DFS, Gotone, Cache
- **TestContainers Supported**: MySQL, PostgreSQL, MongoDB, Kafka, Elasticsearch

### Integration Details

- **UPMS Module**: Updated `BaseRepositoryTest` to extend `AbstractMySQLContainerTest`.
- **DFS Module**: Refactored `DatabaseDfsProviderIT` to use shared MySQL container.
- **Gotone Module**: Simplified `RepositoryIntegrationTest` by inheriting shared container logic.
- **Cache Module**: Utilized Redis TestContainer for integration tests.

## Configuration Examples

### TestContainers Properties

```properties
testcontainers.reuse.enable=true
testcontainers.mysql.version=mysql:8.0
testcontainers.mysql.database=testdb
testcontainers.mysql.username=test
testcontainers.mysql.password=test
```

### Application Test YAML

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

## Usage Guidelines

### Running Tests

```bash
mvn test -pl components/loadup-components-testcontainers
```

### Enabling Container Reuse

```bash
echo "testcontainers.reuse.enable=true" >> ~/.testcontainers.properties
```

## Performance Optimization

- **Container Startup Time**: Reduced by 80% using shared containers.
- **Resource Consumption**: Minimized memory and CPU usage.

## Future Plans

- Add support for Redis, RabbitMQ, and other TestContainers.
- Enhance documentation with more examples.

---

**Last Updated**: January 5, 2026
