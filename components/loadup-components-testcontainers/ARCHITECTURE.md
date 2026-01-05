# `loadup-components-testcontainers` 模块架构概述

---

## 模块目的

`loadup-components-testcontainers` 模块为跨多个模块的集成测试提供共享的 TestContainers。它集中管理容器，遵循 DRY 原则，并通过容器复用显著提高测试性能。

## 支持的容器

### 📊 完整容器列表（7种类型）

| 分类          | 容器              | 默认版本                 | 包路径                         |
|-------------|-----------------|----------------------|-----------------------------|
| **📦 数据库**  | MySQL           | mysql:8.0            | `.testcontainers`           |
|             | PostgreSQL      | postgres:15-alpine   | `.testcontainers.database`  |
|             | MongoDB         | mongo:7.0            | `.testcontainers.database`  |
| **🔴 缓存**   | Redis           | redis:7-alpine       | `.testcontainers`           |
| **📨 消息队列** | Kafka           | cp-kafka:7.5.0       | `.testcontainers.messaging` |
| **🔍 搜索引擎** | Elasticsearch   | elasticsearch:8.11.0 | `.testcontainers.search`    |
| **☁️ 云服务**  | LocalStack (S3) | localstack:3.0       | `.testcontainers`           |

---

## 设计原则

### 核心原则

1. **单一职责原则** - 一个模块管理所有测试容器
2. **依赖倒置原则** - 业务模块依赖抽象的容器接口
3. **开闭原则** - 易于扩展新的容器类型
4. **DRY 原则** - 避免重复的容器管理代码
5. **可复用性** - 共享容器减少冗余
6. **高性能** - 优化启动和资源使用（性能提升 80-90%）
7. **可扩展性** - 易于扩展支持新容器

---

## 三层架构模式

每种容器类型都遵循相同的三层架构：

### 第一层：共享容器类（Shared*Container）

**职责：**

- 管理容器生命周期
- 提供容器访问接口
- 实现单例模式
- 首次访问时自动启动
- 注册 JVM 关闭钩子

**示例：**

```java
public class SharedRedisContainer {
    private static final GenericContainer<?> REDIS_CONTAINER;

    static {
        REDIS_CONTAINER = new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379)
                .withReuse(true);
        REDIS_CONTAINER.start();

        Runtime.getRuntime().addShutdownHook(new Thread(REDIS_CONTAINER::stop));
    }

    public static String getHost() {return REDIS_CONTAINER.getHost();}

    public static Integer getPort() {return REDIS_CONTAINER.getMappedPort(6379);}
}
```

### 第二层：初始化器类（*ContainerInitializer）

**职责：**

- Spring Boot 集成
- 自动配置属性
- 注入测试环境设置

**示例：**

```java
public class RedisContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        TestPropertyValues.of(
                "spring.redis.host=" + SharedRedisContainer.getHost(),
                "spring.redis.port=" + SharedRedisContainer.getPort()
        ).applyTo(context.getEnvironment());
    }
}
```

### 第三层：抽象基类（Abstract*ContainerTest）

**职责：**

- 简化测试编写
- 自动应用初始化器
- 提供便捷方法

**示例：**

```java

@ContextConfiguration(initializers = RedisContainerInitializer.class)
public abstract class AbstractRedisContainerTest {
    protected static String getRedisHost() {
        return SharedRedisContainer.getHost();
    }

    protected static Integer getRedisPort() {
        return SharedRedisContainer.getPort();
    }
}
```

---

## 包结构

```
loadup-components-testcontainers/
├── pom.xml                                    # 集中依赖管理
│
├── src/main/java/.../testcontainers/
│   │
│   ├── 【根包 - 向后兼容】
│   │   ├── SharedMySQLContainer.java
│   │   ├── MySQLContainerInitializer.java
│   │   ├── AbstractMySQLContainerTest.java
│   │   ├── SharedRedisContainer.java
│   │   ├── RedisContainerInitializer.java
│   │   ├── AbstractRedisContainerTest.java
│   │   ├── SharedLocalStackContainer.java
│   │   ├── LocalStackContainerInitializer.java
│   │   └── AbstractLocalStackContainerTest.java
│   │
│   ├── 【database/】
│   │   ├── SharedPostgreSQLContainer.java
│   │   ├── PostgreSQLContainerInitializer.java
│   │   ├── AbstractPostgreSQLContainerTest.java
│   │   ├── SharedMongoDBContainer.java
│   │   ├── MongoDBContainerInitializer.java
│   │   └── AbstractMongoDBContainerTest.java
│   │
│   ├── 【messaging/】
│   │   ├── SharedKafkaContainer.java
│   │   ├── KafkaContainerInitializer.java
│   │   └── AbstractKafkaContainerTest.java
│   │
│   └── 【search/】
│       ├── SharedElasticsearchContainer.java
│       ├── ElasticsearchContainerInitializer.java
│       └── AbstractElasticsearchContainerTest.java
│
└── src/test/java/
    └── [每个容器的集成测试]
```

---

## 集成点

### 模块集成

- **UPMS 模块**：使用 MySQL TestContainer 的仓储测试
- **DFS 模块**：使用 MySQL/PostgreSQL 的数据库提供者测试
- **Gotone 模块**：使用多个容器的集成测试
- **Cache 模块**：Redis 和 Caffeine 测试
- **Scheduler 模块**：潜在的 Kafka 集成

### 跨模块优势

1. **一致的测试环境** - 所有模块使用相同的容器基础设施
2. **更快的测试执行** - 共享容器减少启动开销
3. **资源效率** - 每种类型的容器在所有测试中只有一个实例
4. **易于维护** - 集中的容器配置和版本管理

---

## 配置架构

### 配置层次

1. **默认值**（代码中）
2. **系统属性**（-D 标志）
3. **testcontainers.properties**（在 ~/.testcontainers.properties）
4. **Maven 配置**（在 pom.xml）

### 配置示例

#### 系统属性

```bash
-Dtestcontainers.mysql.version=mysql:8.0
-Dtestcontainers.mysql.database=testdb
-Dtestcontainers.mysql.username=test
-Dtestcontainers.mysql.password=test
```

#### 容器复用

```properties
# ~/.testcontainers.properties
testcontainers.reuse.enable=true
```

---

## 性能架构

### 容器启动优化

| 容器            | 传统方式（每次测试） | 共享方式（首次+后续） | 性能提升       |
|---------------|------------|-------------|------------|
| MySQL         | ~8秒        | 8秒 + 1秒     | **87% ⬆️** |
| PostgreSQL    | ~6秒        | 6秒 + 1秒     | **83% ⬆️** |
| MongoDB       | ~5秒        | 5秒 + <1秒    | **90% ⬆️** |
| Redis         | ~3秒        | 3秒 + <0.5秒  | **90% ⬆️** |
| Kafka         | ~20秒       | 20秒 + 2秒    | **90% ⬆️** |
| Elasticsearch | ~25秒       | 25秒 + 2秒    | **92% ⬆️** |
| LocalStack    | ~15秒       | 15秒 + 1秒    | **93% ⬆️** |

### 实际场景

**10个测试类使用 MySQL：**

- 传统方式：10 × 8秒 = 80 秒
- 共享方式：8秒 + 9×1秒 = 17 秒
- **提升：79% ⬆️**

**完整技术栈（所有容器）：**

- 传统方式：10个类 × 82秒 = 820 秒（13.7 分钟）
- 共享方式：82秒 + 9×7秒 = 145 秒（2.4 分钟）
- **提升：82% ⬆️**

---

## 可扩展性

### 添加新容器类型

要添加新的容器类型，请按照以下步骤操作：

1. **创建共享容器类**
   ```java
   public class SharedNewContainer {
       private static final GenericContainer<?> CONTAINER;
       static { /* 初始化 */ }
       public static String getConnectionUrl() { /* ... */ }
   }
   ```

2. **创建初始化器类**
   ```java
   public class NewContainerInitializer 
           implements ApplicationContextInitializer<ConfigurableApplicationContext> {
       @Override
       public void initialize(ConfigurableApplicationContext context) {
           // 设置属性
       }
   }
   ```

3. **创建抽象基类**
   ```java
   @ContextConfiguration(initializers = NewContainerInitializer.class)
   public abstract class AbstractNewContainerTest {
       // 辅助方法
   }
   ```

4. **添加依赖**到 pom.xml
5. **在 README.md 中记录**使用方法

---

## 最佳实践

### 模块开发者

1. **优先继承** - 继承 Abstract*ContainerTest 以简化使用
2. **启用复用** - 设置 `testcontainers.reuse.enable=true`
3. **清理状态** - 使用 `@BeforeEach` 在测试间重置数据
4. **使用事务** - 利用 `@Transactional` 自动回滚
5. **优化测试** - 将相关测试分组以最大化容器共享

### 容器管理

1. **版本固定** - 指定确切的容器版本以确保可重现性
2. **资源限制** - 配置适当的内存/CPU 限制
3. **网络隔离** - 为多容器测试使用 Docker 网络
4. **日志记录** - 配置适当的日志级别以调试问题
5. **CI/CD 集成** - 预拉取镜像以加速流水线执行

---

## 安全考虑

1. **网络暴露** - 容器仅绑定到本地主机
2. **凭据** - 默认测试凭据是众所周知的（不用于生产环境）
3. **数据隔离** - 每个容器实例都是隔离的
4. **清理** - 容器在测试完成后删除（或在 JVM 关闭时）

---

## 未来增强

### 计划功能

- [ ] 添加 RabbitMQ 支持用于 AMQP 消息传递
- [ ] 添加 Cassandra 支持用于宽列数据库
- [ ] 添加 MinIO 支持作为 LocalStack 的替代方案
- [ ] 健康检查集成以加快测试启动
- [ ] 自定义 Docker Compose 文件支持
- [ ] 自动模式迁移支持
- [ ] 容器指标和监控
- [ ] 测试数据种子工具

### CI/CD 集成

- [ ] GitHub Actions 工作流模板
- [ ] GitLab CI 配置示例
- [ ] Jenkins 流水线集成指南
- [ ] 预构建 Docker 镜像缓存策略

---

## 故障排除

### 常见问题

**Docker 未运行**

```bash
# macOS
open -a Docker

# 验证
docker info
```

**端口冲突**

```bash
# 查找并停止冲突的进程
lsof -i :3306  # MySQL
lsof -i :6379  # Redis
```

**容器启动超时**

```java
// 在测试配置中增加超时时间
@SpringBootTest(properties = {
        "spring.test.context.cache.maxSize=1",
        "testcontainers.startup.timeout=120"
})
```

**内存问题**

```bash
# 增加 Docker 内存限制（Docker Desktop）
# 偏好设置 → 资源 → 内存 → 4GB+
```

---

## 依赖项

### 核心依赖

| 依赖项                          | 版本     | 用途                |
|------------------------------|--------|-------------------|
| testcontainers-core          | 1.19.3 | TestContainers 框架 |
| testcontainers-mysql         | 1.19.3 | MySQL 支持          |
| testcontainers-postgresql    | 1.19.3 | PostgreSQL 支持     |
| testcontainers-mongodb       | 1.19.3 | MongoDB 支持        |
| testcontainers-kafka         | 1.19.3 | Kafka 支持          |
| testcontainers-elasticsearch | 1.19.3 | Elasticsearch 支持  |
| testcontainers-localstack    | 1.19.3 | LocalStack 支持     |
| spring-boot-test             | 3.x    | Spring Boot 测试支持  |

---

## 许可证

本模块遵循 Apache License 2.0 协议。

---

**架构版本**：2.0  
**最后更新**：2026年1月5日  
**维护者**：LoadUp Framework Team
