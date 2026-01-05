# UPMS Module Test Suite

本模块为 UPMS 模块提供完整的测试套件，包括单元测试和集成测试。

## 环境要求

- Java 17+
- Maven 3.6+
- Docker (用于 Testcontainers)

## 使用 Testcontainers 运行测试

本测试套件使用 **Testcontainers** 自动管理 MySQL 容器，无需手动启动 Docker 容器。

### 前置条件

确保你已经安装了 Docker 并且 Docker 守护进程正在运行：

```bash
docker --version
docker ps
```

### 运行测试

Testcontainers 会在测试运行时自动启动和停止 MySQL 容器：

```bash
# 运行所有测试
mvn clean test

# 运行单个测试类
mvn test -Dtest=UserRepositoryTest

# 运行集成测试
mvn verify

# 跳过测试（编译）
mvn clean install -DskipTests
```

### Testcontainers 工作原理

1. **自动容器管理**：测试开始时自动启动 MySQL 8.0 容器
2. **容器重用**：通过 `.withReuse(true)` 配置，多次运行测试时重用同一容器（需配置 `~/.testcontainers.properties`）
3. **自动清理**：测试结束后自动停止和清理容器
4. **动态配置**：数据库连接信息自动注入到 Spring 配置中

### 优化 Testcontainers 性能

为了提高测试速度，可以启用容器重用功能：

1. 创建文件 `~/.testcontainers.properties`：

```properties
testcontainers.reuse.enable=true
```

2. 这样多次运行测试时会重用同一个 MySQL 容器，大幅提升速度

## 测试配置

### 数据库配置

测试使用 MySQL 8.0 数据库（通过 Testcontainers），配置文件位于：

- `src/test/resources/application-test.yml` - Spring Boot 测试配置
- `src/test/resources/schema-test.sql` - 数据库表结构（MySQL 语法）
- `src/test/resources/test-data.sql` - 测试数据

### HikariCP 连接池配置

连接池配置已在 `application-test.yml` 中设置：

```yaml
spring:
  datasource:
    hikari:
      pool-name: LoadupTestPool
      minimum-idle: 2
      maximum-pool-size: 10
      connection-timeout: 30000
      idle-timeout: 30000
      max-lifetime: 1800000
```

## 测试覆盖率

本项目使用 JaCoCo 生成测试覆盖率报告。

运行测试后，查看覆盖率报告：

```bash
# 生成报告
mvn clean test

# 查看报告
open target/site/jacoco/index.html
```

覆盖率要求：

- 行覆盖率：≥ 90%
- 分支覆盖率：≥ 85%

## 测试结构

```
src/test/java/
├── com.github.loadup.modules.upms/
│   ├── UpmsTestApplication.java          # 测试应用入口
│   └── repository/                       # Repository 测试
│       ├── BaseRepositoryTest.java       # 基础测试类（含 Testcontainers 配置）
│       ├── UserRepositoryTest.java       # 用户仓储测试
│       ├── RoleRepositoryTest.java       # 角色仓储测试
│       ├── DepartmentRepositoryTest.java # 部门仓储测试
│       ├── PermissionRepositoryTest.java # 权限仓储测试
│       ├── LoginLogRepositoryTest.java   # 登录日志测试
│       └── OperationLogRepositoryTest.java # 操作日志测试

src/test/resources/
├── application-test.yml    # 测试配置
├── schema-test.sql         # MySQL 表结构
└── test-data.sql          # 测试数据
```

## 故障排查

### Docker 未运行

如果遇到类似错误：

```
Could not find a valid Docker environment
```

请确保 Docker 已启动：

```bash
# macOS/Linux
sudo systemctl start docker

# macOS with Docker Desktop
open -a Docker
```

### 端口冲突

Testcontainers 会自动分配端口，不会有端口冲突问题。

### 容器启动慢

首次运行时会下载 MySQL 镜像，可能需要几分钟。后续运行会快很多。

启用容器重用可以进一步提升速度（见上文"优化 Testcontainers 性能"）。

### 查看容器日志

如果测试失败，可以查看容器日志：

```bash
# 查看正在运行的容器
docker ps

# 查看容器日志
docker logs <container_id>
```

## 注意事项

1. 测试使用 `@Transactional` 注解，每个测试方法执行后自动回滚
2. 测试数据在每次测试运行前会重新初始化
3. 不要在测试中依赖数据的持久化状态
4. Testcontainers 需要 Docker 环境，CI/CD 环境需要支持 Docker-in-Docker

## 技术栈

- **测试框架**: JUnit 5
- **断言库**: AssertJ
- **Mock 框架**: Mockito
- **容器管理**: Testcontainers
- **数据库**: MySQL 8.0
- **连接池**: HikariCP
- **ORM 框架**: MyBatis-Flex
- **覆盖率**: JaCoCo

