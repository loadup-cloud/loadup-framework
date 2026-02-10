# LoadUp Testify - Testcontainers Integration

LoadUp Testify 提供了与 Testcontainers 的无缝集成，支持多种容器类型和灵活的配置方式。

## 特性

- ✅ **多种容器支持**: MySQL, PostgreSQL, MongoDB, Redis, Kafka, Elasticsearch, LocalStack
- ✅ **灵活配置**: 支持注解方式和配置文件方式
- ✅ **容器复用**: 支持跨测试类复用容器，提升测试速度
- ✅ **自动配置**: 自动将容器连接信息注入 Spring 环境
- ✅ **编程式 API**: 支持 @Testcontainers 注解和手动容器管理

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-testify-spring-boot-starter</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### 2. 使用方式

#### 方式 A: 注解方式（推荐）

```java
import io.github.loadup.testify.starter.annotation.EnableTestContainers;
import io.github.loadup.testify.starter.annotation.ContainerType;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest
@EnableTestContainers(ContainerType.MYSQL)
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testCreateUser() {
        // 测试代码
        // MySQL 容器已自动启动，数据源已自动配置
    }
}
```

#### 方式 B: 配置文件方式

**application-test.yml:**

```yaml
loadup:
  testcontainers:
    enabled: true
    reuse: true
    mysql:
      enabled: true
      image: mysql:8.0
      database: testdb
      username: test
      password: test
```

**测试类:**

```java
@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testCreateUser() {
        // 测试代码
    }
}
```

#### 方式 C: 编程式（Testcontainers 原生方式）

```java
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@Testcontainers
class UserServiceTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
    
    @Test
    void testCreateUser() {
        // 测试代码
    }
}
```

## 支持的容器类型

### 数据库容器

#### MySQL

**注解方式:**
```java
@EnableTestContainers(ContainerType.MYSQL)
```

**配置方式:**
```yaml
loadup:
  testcontainers:
    mysql:
      enabled: true
      image: mysql:8.0
      database: testdb
      username: test
      password: test
```

**自动注入的属性:**
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.datasource.driver-class-name`

#### PostgreSQL

**注解方式:**
```java
@EnableTestContainers(ContainerType.POSTGRESQL)
```

**配置方式:**
```yaml
loadup:
  testcontainers:
    postgresql:
      enabled: true
      image: postgres:14
      database: testdb
      username: test
      password: test
```

#### MongoDB

**注解方式:**
```java
@EnableTestContainers(ContainerType.MONGODB)
```

**配置方式:**
```yaml
loadup:
  testcontainers:
    mongo:
      enabled: true
      image: mongo:6.0
```

**自动注入的属性:**
- `spring.data.mongodb.uri`

### 缓存容器

#### Redis

**注解方式:**
```java
@EnableTestContainers(ContainerType.REDIS)
```

**配置方式:**
```yaml
loadup:
  testcontainers:
    redis:
      enabled: true
      image: redis:7-alpine
```

### 消息队列容器

#### Kafka

**注解方式:**
```java
@EnableTestContainers(ContainerType.KAFKA)
```

**配置方式:**
```yaml
loadup:
  testcontainers:
    kafka:
      enabled: true
      image: confluentinc/cp-kafka:latest
```

**自动注入的属性:**
- `spring.kafka.bootstrap-servers`

### 搜索引擎容器

#### Elasticsearch

**注解方式:**
```java
@EnableTestContainers(ContainerType.ELASTICSEARCH)
```

**配置方式:**
```yaml
loadup:
  testcontainers:
    elasticsearch:
      enabled: true
      image: elasticsearch:8.7.0
```

**自动注入的属性:**
- `spring.elasticsearch.uris`

### 云服务容器

#### LocalStack (AWS 服务模拟)

**注解方式:**
```java
@EnableTestContainers(ContainerType.LOCALSTACK)
```

**配置方式:**
```yaml
loadup:
  testcontainers:
    localstack:
      enabled: true
      services: S3,SQS,SNS
```

## 多容器组合

### 注解方式

```java
@SpringBootTest
@EnableTestContainers({
    ContainerType.MYSQL,
    ContainerType.REDIS,
    ContainerType.KAFKA
})
class IntegrationTest {
    // 测试代码
}
```

### 配置方式

```yaml
loadup:
  testcontainers:
    enabled: true
    reuse: true
    mysql:
      enabled: true
      image: mysql:8.0
    redis:
      enabled: true
      image: redis:7-alpine
    kafka:
      enabled: true
```

## 容器复用

容器复用可以显著提高测试速度，避免每次测试都重启容器。

### 启用复用

**注解方式:**
```java
@EnableTestContainers(value = ContainerType.MYSQL, reuse = true)
```

**配置方式:**
```yaml
loadup:
  testcontainers:
    reuse: true
```

### 注意事项

1. 容器复用需要在 `~/.testcontainers.properties` 中启用:
   ```properties
   testcontainers.reuse.enable=true
   ```

2. 复用的容器会在 JVM 退出后仍然运行，需要手动停止:
   ```bash
   docker stop $(docker ps -aq --filter label=org.testcontainers)
   ```

## 高级配置

### 自定义容器镜像

```yaml
loadup:
  testcontainers:
    mysql:
      enabled: true
      image: mysql:8.0.32
      database: custom_db
      username: custom_user
      password: custom_pass
```

### 容器启动脚本

```yaml
loadup:
  testcontainers:
    mysql:
      enabled: true
      init-script: classpath:db/init.sql
```

### 容器环境变量

```yaml
loadup:
  testcontainers:
    mongodb:
      enabled: true
      env:
        MONGO_INITDB_ROOT_USERNAME: admin
        MONGO_INITDB_ROOT_PASSWORD: password
```

## 最佳实践

### 1. 使用共享容器

对于耗时的容器（如 Elasticsearch），建议使用共享容器：

```java
@SpringBootTest
@EnableTestContainers(value = ContainerType.ELASTICSEARCH, reuse = true)
class SearchTest {
    // 所有测试类共享同一个 Elasticsearch 容器
}
```

### 2. 测试数据隔离

每个测试方法前清理数据库：

```java
@BeforeEach
void setUp() {
    jdbcTemplate.execute("TRUNCATE TABLE users");
}
```

### 3. 使用 TestNG 继承基类

```java
public class DatabaseTestBase extends TestifyBase {
    // 所有数据库测试继承此类
}

@EnableTestContainers(ContainerType.MYSQL)
class UserServiceTest extends DatabaseTestBase {
    // 测试代码
}
```

### 4. CI/CD 环境优化

在 CI 环境中禁用容器复用，避免状态污染：

```yaml
# application-ci.yml
loadup:
  testcontainers:
    reuse: false
```

## 故障排查

### 问题 1: 容器启动失败

**原因:** Docker 守护进程未运行或配置错误

**解决方案:**
```bash
# 检查 Docker 状态
docker ps

# 检查 Testcontainers 配置
cat ~/.testcontainers.properties
```

### 问题 2: 端口冲突

**原因:** 本地已有服务占用端口

**解决方案:** Testcontainers 会自动分配随机端口，无需手动配置

### 问题 3: 容器无法访问网络

**原因:** Docker 网络配置问题

**解决方案:**
```bash
# 重启 Docker 守护进程
# macOS: 从菜单栏重启 Docker Desktop
# Linux: sudo systemctl restart docker
```

## 参考资料

- [Testcontainers 官方文档](https://www.testcontainers.org/)
- [LoadUp Components Testcontainers](../../loadup-components/loadup-components-testcontainers/README.md)
- [Spring Boot 测试指南](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

## 更新日志

### v0.0.2

- ✅ 移除 `loadup-testify-infra-container` 模块
- ✅ 集成 `loadup-components-testcontainers`
- ✅ 新增 `@EnableTestContainers` 注解支持
- ✅ 简化配置项，移除 services 配置
- ✅ 支持多种初始化方式（注解/配置/编程式）

### v0.0.1

- 初始版本
- 基于 infra-container 实现
