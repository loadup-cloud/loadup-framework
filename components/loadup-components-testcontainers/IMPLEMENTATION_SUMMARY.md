# LoadUp Components TestContainers - 实现总结

## 项目概述

成功创建了 `loadup-components-testcontainers` 模块，这是一个专门用于测试的基础组件，提供了共享的 MySQL TestContainer
实例，可以在多个测试类之间复用，显著提高测试执行效率。

## 项目结构

```
loadup-components-testcontainers/
├── pom.xml                           # Maven 配置文件
├── README.md                         # 项目说明文档
├── USAGE_EXAMPLES.md                 # 详细使用示例
├── CONFIGURATION_EXAMPLES.md         # 配置示例
└── src/
    ├── main/
    │   └── java/
    │       └── com/github/loadup/components/testcontainers/
    │           ├── SharedMySQLContainer.java            # 共享 MySQL 容器类
    │           ├── MySQLContainerInitializer.java       # Spring Boot 上下文初始化器
    │           └── AbstractMySQLContainerTest.java      # 抽象测试基类
    └── test/
        └── java/
            └── com/github/loadup/components/testcontainers/
                └── SharedMySQLContainerTest.java        # 单元测试
```

## 核心组件

### 1. SharedMySQLContainer

**功能特性：**

- 单例模式的 MySQL 容器，所有测试共享同一个实例
- 自动启动和管理 MySQL Docker 容器
- 支持通过系统属性自定义配置
- 提供便捷的访问方法获取连接信息
- 自动添加 JVM 关闭钩子清理资源

**主要 API：**

- `getInstance()` - 获取共享容器实例
- `getJdbcUrl()` - 获取 JDBC URL
- `getUsername()` - 获取用户名
- `getPassword()` - 获取密码
- `getDatabaseName()` - 获取数据库名
- `getHost()` / `getMappedPort()` - 获取主机和端口

**配置选项：**

- `testcontainers.mysql.version` - MySQL 版本（默认: mysql:8.0）
- `testcontainers.mysql.database` - 数据库名（默认: testdb）
- `testcontainers.mysql.username` - 用户名（默认: test）
- `testcontainers.mysql.password` - 密码（默认: test）

### 2. MySQLContainerInitializer

**功能特性：**

- Spring Boot ApplicationContextInitializer 实现
- 自动配置 Spring Boot 数据源属性
- 无需手动配置连接信息

**使用方式：**

```java

@SpringBootTest
@ContextConfiguration(initializers = MySQLContainerInitializer.class)
class MyTest {
    // 测试代码
}
```

### 3. AbstractMySQLContainerTest

**功能特性：**

- 抽象基类，简化测试编写
- 自动应用 MySQLContainerInitializer
- 提供便捷方法访问容器属性

**使用方式：**

```java

@SpringBootTest
class MyTest extends AbstractMySQLContainerTest {
    // 测试代码
}
```

## 依赖管理

### 主要依赖

| 依赖                   | 版本     | 说明                 |
|----------------------|--------|--------------------|
| testcontainers       | 1.19.3 | TestContainers 核心库 |
| testcontainers-mysql | 1.19.3 | MySQL 模块           |
| mysql-connector-j    | 最新     | MySQL JDBC 驱动      |
| spring-boot-test     | 可选     | Spring Boot 测试支持   |
| spring-context       | 可选     | Spring 上下文支持       |
| spring-test          | 可选     | Spring 测试支持        |
| junit-jupiter        | 测试     | JUnit 5 测试框架       |

### 使用方式

在其他模块的 `pom.xml` 中添加：

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

## 使用场景

### 1. 基本 JDBC 测试

直接使用 SharedMySQLContainer 获取连接信息

### 2. Spring Boot 集成测试

使用 MySQLContainerInitializer 或 AbstractMySQLContainerTest

### 3. JPA Repository 测试

配合 @DataJpaTest 使用

### 4. Liquibase/Flyway 集成测试

验证数据库迁移脚本

### 5. 多模块集成测试

在不同模块间共享同一个测试数据库实例

## 性能优化

### 容器共享策略

- **单例模式**：所有测试类共享同一个容器实例
- **容器复用**：支持 TestContainers 的容器复用功能
- **延迟初始化**：只在首次访问时启动容器

### 优势对比

| 策略            | 启动时间  | 隔离性   | 资源消耗  |
|---------------|-------|-------|-------|
| 每个测试类一个容器     | 慢     | 高     | 高     |
| **共享容器（本实现）** | **快** | **中** | **低** |
| 外部数据库         | 最快    | 低     | 外部    |

### 测试数据隔离建议

虽然容器是共享的，但建议：

- 在 `@BeforeEach` 中清理数据
- 使用 `@Transactional` 自动回滚
- 使用唯一的测试数据标识

## 最佳实践

### 1. 测试类组织

```java

@SpringBootTest
class UserRepositoryTest extends AbstractMySQLContainerTest {

    @BeforeEach
    void setUp() {
        // 清理测试数据
    }

    @Test
    void testCase() {
        // 测试逻辑
    }
}
```

### 2. CI/CD 集成

- 确保 CI 环境支持 Docker
- 使用缓存加速镜像下载
- 配置合理的超时时间

### 3. 本地开发

- 启用容器复用功能
- 使用本地 Docker 镜像
- 配置合理的日志级别

## 文档清单

1. **README.md** - 项目概述和快速开始
2. **USAGE_EXAMPLES.md** - 详细的使用示例
3. **CONFIGURATION_EXAMPLES.md** - 各种环境的配置示例
4. **本文档** - 实现总结和技术细节

## 测试验证

模块包含完整的单元测试：

- 容器启动和运行验证
- 容器属性验证
- 数据库连接测试
- 表创建和数据操作测试
- 容器单例验证

## 后续扩展建议

### 短期扩展

1. 添加 PostgreSQL 支持
2. 添加 Redis 容器支持
3. 添加 MongoDB 容器支持

### 中期扩展

1. 支持多数据库类型组合
2. 添加容器健康检查
3. 提供自定义初始化脚本支持

### 长期扩展

1. 支持分布式测试环境
2. 集成性能监控
3. 提供测试数据生成工具

## 集成到现有模块

### loadup-components-database

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

测试类示例：

```java

@SpringBootTest
class DatabaseComponentTest extends AbstractMySQLContainerTest {
    @Autowired
    private DataSource dataSource;

    @Test
    void testDataSource() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertNotNull(conn);
        }
    }
}
```

### loadup-components-liquibase

验证 Liquibase 迁移脚本：

```java

@SpringBootTest
class LiquibaseMigrationTest extends AbstractMySQLContainerTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testMigration() {
        // 验证表是否创建成功
    }
}
```

## 常见问题解决

### 1. Docker 未运行

**错误**: `Could not find a valid Docker environment`
**解决**: 确保 Docker Desktop 已启动

### 2. 端口冲突

**错误**: `Port 3306 already in use`
**解决**: TestContainers 会自动分配随机端口，不会冲突

### 3. 镜像下载慢

**解决**:

- 配置 Docker 镜像加速器
- 提前拉取镜像: `docker pull mysql:8.0`

### 4. 内存不足

**解决**: 增加 Docker Desktop 内存限制

## 版本信息

- **初始版本**: 1.0.0-SNAPSHOT
- **Java 版本**: 17+
- **Spring Boot 版本**: 3.1.2+
- **TestContainers 版本**: 1.19.3
- **MySQL 默认版本**: 8.0

## 贡献指南

欢迎提交 Issue 和 Pull Request！

贡献内容可以包括：

- Bug 修复
- 新功能实现
- 文档改进
- 测试用例补充

## 许可证

Apache License 2.0

## 联系方式

- GitHub: https://github.com/loadup-cloud/loadup-framework
- Issues: https://github.com/loadup-cloud/loadup-framework/issues

---

**状态**: ✅ 已完成
**最后更新**: 2026-01-05

