# LoadUp Components TestContainers - 架构设计文档

## 📋 概述

`loadup-components-testcontainers` 模块提供企业级的 TestContainers 共享容器功能，支持在 TestContainers 和实际服务之间灵活切换，适用于不同的测试场景。

### 核心价值

1. **灵活性**：TestContainers ↔️ 实际服务自由切换
2. **高性能**：共享容器实例，测试速度提升 80-90%
3. **易用性**：配置驱动，零代码修改
4. **可靠性**：统一架构，生产级质量

---

## 🏗️ 架构设计

### 三层架构模式

所有容器遵循统一的三层架构：

```
┌───────────────────────────────────────────────────────────┐
│                   Layer 3: AbstractTest                    │
│                      (测试基类层)                          │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  @ContextConfiguration(                             │  │
│  │      initializers = XXXContainerInitializer.class   │  │
│  │  )                                                   │  │
│  │  public abstract class AbstractXXXContainerTest {   │  │
│  │      // 提供便捷方法                                │  │
│  │  }                                                   │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────────┐
│                Layer 2: Initializer                        │
│                   (初始化器层)                            │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  public class XXXContainerInitializer               │  │
│  │      implements ApplicationContextInitializer {     │  │
│  │                                                      │  │
│  │    @Override                                         │  │
│  │    public void initialize(context) {                │  │
│  │      // 检查配置                                    │  │
│  │      if (enabled) {                                 │  │
│  │        // 注入 TestContainer 属性                  │  │
│  │      } else {                                       │  │
│  │        // 使用实际服务配置                         │  │
│  │      }                                              │  │
│  │    }                                                │  │
│  │  }                                                  │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────────┐
│              Layer 1: SharedContainer                      │
│                  (共享容器层)                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  public class SharedXXXContainer {                  │  │
│  │    private static final boolean ENABLED;            │  │
│  │    private static final Container CONTAINER;        │  │
│  │                                                      │  │
│  │    static {                                         │  │
│  │      ENABLED = checkConfig();                      │  │
│  │      if (ENABLED) {                                │  │
│  │        CONTAINER = startContainer();               │  │
│  │      } else {                                      │  │
│  │        CONTAINER = null;                           │  │
│  │      }                                             │  │
│  │    }                                               │  │
│  │                                                     │  │
│  │    public static boolean isEnabled() { ... }       │  │
│  │    public static String getXXX() { ... }          │  │
│  │  }                                                 │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
```

---

## 🎯 核心机制

### 1. 条件启动机制

**决策流程：**

```
应用启动
    ↓
SharedContainer 静态初始化
    ↓
读取系统属性: loadup.testcontainers.{type}.enabled
    ↓
    ├─ true (默认)
    │    ↓
    │  创建并启动容器
    │    ↓
    │  记录连接信息
    │    ↓
    │  注册关闭钩子
    │
    └─ false
         ↓
       设置 CONTAINER = null
         ↓
       等待从配置文件读取实际服务配置
```

**代码实现：**

```java
public class SharedMySQLContainer {
    private static final boolean        ENABLED;
    private static final MySQLContainer CONTAINER;

    static {
        // 检查是否启用
        ENABLED = Boolean.parseBoolean(
                System.getProperty("loadup.testcontainers.mysql.enabled", "true")
        );

        if (ENABLED) {
            // 启动容器
            CONTAINER = new MySQLContainer(...)
                .withReuse(true);
            CONTAINER.start();
        } else {
            // 禁用模式
            CONTAINER = null;
        }
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static String getJdbcUrl() {
        if (!ENABLED) {
            throw new IllegalStateException(
                    "MySQL TestContainer is disabled. " +
                            "Please configure spring.datasource.url in application.yml"
            );
        }
        return JDBC_URL;
    }
}
```

### 2. 初始化器条件注入

**工作流程：**

```
Spring 测试启动
    ↓
执行 ApplicationContextInitializer
    ↓
读取 application.yml 配置: loadup.testcontainers.{type}.enabled
    ↓
    ├─ enabled = true
    │    ↓
    │  调用 SharedContainer.getXXX()
    │    ↓
    │  注入容器连接属性到 Environment
    │    ↓
    │  TestPropertyValues.of(
    │      "spring.datasource.url=" + container.getJdbcUrl()
    │  ).applyTo(context)
    │
    └─ enabled = false
         ↓
       跳过属性注入
         ↓
       使用 application.yml 中的实际服务配置
```

**代码实现：**

```java

@Slf4j
public class MySQLContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        String enabled = context.getEnvironment()
                .getProperty("loadup.testcontainers.mysql.enabled", "true");

        if (Boolean.parseBoolean(enabled)) {
            // TestContainers 模式
            log.info("Using MySQL TestContainer for tests");
            TestPropertyValues.of(
                    "spring.datasource.url=" + SharedMySQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + SharedMySQLContainer.getUsername(),
                    "spring.datasource.password=" + SharedMySQLContainer.getPassword()
            ).applyTo(context.getEnvironment());
        } else {
            // 实际服务模式
            log.info("Using real MySQL database from application configuration");
            // 不注入任何属性，使用配置文件中的配置
        }
    }
}
```

### 3. 配置优先级

```
系统属性 (-D)
    ↓ (优先级最高)
环境变量 (export)
    ↓
application-{profile}.yml
    ↓
application.yml
    ↓
代码默认值 (true)
    ↓ (优先级最低)
```

**示例：**

```bash
# 方式 1: 系统属性（最高优先级）
mvn test -Dloadup.testcontainers.mysql.enabled=false

# 方式 2: 环境变量
export LOADUP_TESTCONTAINERS_MYSQL_ENABLED=false
mvn test

# 方式 3: application.yml
# loadup:
#   testcontainers:
#     mysql:
#       enabled: false
```

---

## 📦 容器分类架构

### 数据库类容器 (Database)

**包含：** MySQL, PostgreSQL, MongoDB

**共同特征：**

- 需要连接 URL/字符串
- 需要认证信息（用户名、密码）
- 需要数据库名
- 提供 JDBC/Connection String

**配置属性：**

```yaml
loadup:
  testcontainers:
    mysql:
      enabled: true
      version: mysql:8.0
      database: testdb
      username: test
      password: test
    postgresql:
      enabled: true
      version: postgres:15-alpine
    mongodb:
      enabled: true
      version: mongo:7.0
```

**属性注入：**

```
MySQL: spring.datasource.url, username, password, driver-class-name
PostgreSQL: spring.datasource.url, username, password, driver-class-name
MongoDB: spring.data.mongodb.uri, host, port
```

---

### 缓存类容器 (Cache)

**包含：** Redis

**特征：**

- 简单的 Host + Port 配置
- 无需认证（测试环境）
- 支持多种配置路径

**配置属性：**

```yaml
loadup:
  testcontainers:
    redis:
      enabled: true
      version: redis:7-alpine
```

**属性注入：**

```
spring.redis.host, spring.redis.port
spring.data.redis.host, spring.data.redis.port
loadup.cache.redis.host, loadup.cache.redis.port
```

---

### 消息队列类容器 (Messaging)

**包含：** Kafka

**特征：**

- Bootstrap Servers 配置
- 自动处理依赖（Zookeeper）
- 支持生产者和消费者配置

**配置属性：**

```yaml
loadup:
  testcontainers:
    kafka:
      enabled: true
      version: apache/kafka:4.1.1
```

**属性注入：**

```
spring.kafka.bootstrap-servers
spring.kafka.consumer.bootstrap-servers
spring.kafka.producer.bootstrap-servers
```

---

### 搜索引擎类容器 (Search)

**包含：** Elasticsearch

**特征：**

- HTTP Host Address
- 需要禁用安全配置（测试环境）
- 支持多种客户端配置

**配置属性：**

```yaml
loadup:
  testcontainers:
    elasticsearch:
      enabled: true
      version: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

**属性注入：**

```
spring.elasticsearch.uris
spring.elasticsearch.rest.uris
spring.data.elasticsearch.client.reactive.endpoints
```

---

### 云服务类容器 (Cloud)

**包含：** LocalStack (S3)

**特征：**

- 模拟 AWS 服务
- 提供 Access Key, Secret Key
- 支持多种 AWS SDK 配置

**配置属性：**

```yaml
loadup:
  testcontainers:
    localstack:
      enabled: true
      version: localstack/localstack:3.0
```

**属性注入：**

```
aws.s3.endpoint
aws.access-key-id, aws.secret-access-key, aws.region
cloud.aws.credentials.access-key, cloud.aws.credentials.secret-key
loadup.dfs.s3.endpoint, loadup.dfs.s3.accessKey, loadup.dfs.s3.secretKey
```

---

## 🔄 使用模式

### 模式 1: 纯 TestContainers（默认）

**场景：** 本地开发，快速测试

**配置：**

```yaml
# 无需配置，默认启用
```

**流程：**

```
测试启动
  ↓
SharedContainer 启动
  ↓
Initializer 注入容器属性
  ↓
测试执行
  ↓
测试完成，容器保持运行（复用）
```

---

### 模式 2: 纯实际服务

**场景：** CI 环境，性能测试

**配置：**

```yaml
loadup:
  testcontainers:
    enabled: false

spring:
  datasource:
    url: jdbc:mysql://mysql-server:3306/testdb
    username: ci_user
    password: ci_password
```

**流程：**

```
测试启动
  ↓
SharedContainer 不启动（ENABLED = false）
  ↓
Initializer 跳过容器属性注入
  ↓
使用 application.yml 中的实际服务配置
  ↓
测试执行
```

---

### 模式 3: 混合模式

**场景：** 部分服务容器化，部分使用真实服务

**配置：**

```yaml
loadup:
  testcontainers:
    enabled: true
    mysql:
      enabled: false    # 使用实际 MySQL
    redis:
      enabled: true     # 使用 TestContainers Redis

spring:
  datasource:
    url: jdbc:mysql://dev-mysql:3306/devdb
    username: dev
    password: dev
  # Redis 配置将被 TestContainers 覆盖
```

**流程：**

```
测试启动
  ↓
SharedMySQLContainer 不启动（enabled = false）
SharedRedisContainer 启动（enabled = true）
  ↓
MySQLInitializer 跳过注入，使用配置文件
RedisInitializer 注入容器属性
  ↓
测试执行（MySQL 实际服务 + Redis 容器）
```

---

## 🎨 设计模式

### 单例模式

**目的：** 确保容器实例全局唯一，复用容器

**实现：**

```java
public class SharedMySQLContainer {
    // 静态实例，JVM 级别单例
    private static final MySQLContainer MYSQL_CONTAINER;

    static {
        // 静态代码块初始化，线程安全
        MYSQL_CONTAINER = new MySQLContainer(...);
        MYSQL_CONTAINER.start();
    }

    // 私有构造器，防止实例化
    private SharedMySQLContainer() {
        throw new UnsupportedOperationException();
    }
}
```

---

### 策略模式

**目的：** 根据配置选择不同的属性注入策略

**实现：**

```java
public class MySQLContainerInitializer {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        String enabled = context.getEnvironment()
                .getProperty("loadup.testcontainers.mysql.enabled", "true");

        // 策略选择
        if (Boolean.parseBoolean(enabled)) {
            // 策略 A: TestContainers
            injectContainerProperties(context);
        } else {
            // 策略 B: 实际服务（无操作）
            // 使用配置文件中的配置
        }
    }
}
```

---

### 模板方法模式

**目的：** 所有容器遵循统一的初始化流程

**模板流程：**

```
1. 检查配置 (isEnabled)
2. 创建容器 (createContainer)
3. 启动容器 (start)
4. 记录信息 (log)
5. 注册钩子 (registerShutdownHook)
```

**实现：** 每个 SharedContainer 都实现这个流程

---

## 📊 性能优化

### 1. 容器复用

**机制：** `.withReuse(true)`

**效果：**

- 第一次启动：约 10-30 秒（根据容器类型）
- 后续测试：< 1 秒（复用已启动容器）
- 性能提升：**80-90%**

### 2. 并行初始化

**机制：** 静态代码块并行执行

**效果：** 多个容器可以同时启动

### 3. 延迟加载

**机制：** 只有访问时才触发静态初始化

**效果：** 未使用的容器不会启动

### 4. CI 优化

**机制：** 禁用 TestContainers，使用已有服务

**效果：**

- 跳过容器启动时间
- 利用 CI 环境的服务实例
- 更稳定的测试环境

---

## 🔒 安全设计

### 1. 不可变性

所有连接信息都是 `final` 常量，一旦初始化不可修改。

### 2. 防御式编程

```java
public static String getJdbcUrl() {
    if (!ENABLED) {
        throw new IllegalStateException(
                "Container is disabled. Please configure real service."
        );
    }
    return JDBC_URL;
}
```

### 3. 资源清理

自动注册 JVM 关闭钩子，确保容器正确关闭。

---

## 🧪 测试策略

### 单元测试

测试容器的启用/禁用逻辑：

```java

@Test
void testContainerEnabled() {
    assertTrue(SharedMySQLContainer.isEnabled());
}

@Test
void testContainerDisabled() {
    System.setProperty("loadup.testcontainers.mysql.enabled", "false");
    // 重新加载类
    // 验证 ENABLED = false
}
```

### 集成测试

测试不同配置场景：

```java

@SpringBootTest
@ActiveProfiles("test")
class TestContainersModeTest { ...
}

@SpringBootTest
@ActiveProfiles("ci")
class RealServiceModeTest { ...
}

@SpringBootTest
@ActiveProfiles("mixed")
class MixedModeTest { ...
}
```

---

## 🚀 扩展点

### 1. 新增容器类型

遵循三层架构模式：

1. 创建 `SharedXXXContainer`
2. 创建 `XXXContainerInitializer`
3. 创建 `AbstractXXXContainerTest`

### 2. 自定义配置属性

在 `TestContainersProperties` 中添加新配置。

### 3. 自定义初始化逻辑

扩展 `ApplicationContextInitializer` 接口。

---

## 📈 监控和调试

### 日志输出

```
[INFO] Initializing shared MySQL TestContainer with version: mysql:8.0
[INFO] Shared MySQL TestContainer started successfully
[INFO] JDBC URL: jdbc:mysql://localhost:32768/testdb
[INFO] Using MySQL TestContainer for tests
```

### 检查容器状态

```java
// 检查是否启用
boolean enabled = SharedMySQLContainer.isEnabled();

// 获取容器实例
MySQLContainer container = SharedMySQLContainer.getInstance();

// 检查容器是否运行
boolean running = container.isRunning();
```

---

## 🎯 设计原则总结

1. **单一职责**：每个类只负责一件事
2. **开闭原则**：对扩展开放，对修改关闭
3. **里氏替换**：AbstractTest 可以任意替换
4. **接口隔离**：最小化接口暴露
5. **依赖倒置**：依赖抽象而非具体实现
6. **DRY 原则**：避免重复代码
7. **配置驱动**：行为由配置控制
8. **向后兼容**：保持 API 稳定性

---

## 📚 参考资料

- TestContainers 官方文档: https://www.testcontainers.org/
- Spring Boot 测试文档: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing

---

**版本：** 2.0.0  
**更新日期：** 2026-01-08  
**作者：** LoadUp Framework Team

