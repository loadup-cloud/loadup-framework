# UPMS Module Test Suite

本模块为 UPMS 模块提供完整的测试套件，包括单元测试和集成测试。

## 环境要求

- Java 17+
- Maven 3.6+
- MySQL 8.0+ (使用 devcontainer 或本地安装)

## 使用 DevContainer 运行测试

### 前置条件

确保你已经安装了 Docker 和 Docker Compose。

### 配置步骤

1. **复制环境变量文件**

```bash
cp .env.example .env
```

2. **修改 .env 文件**（如果需要）

根据你的需求修改数据库连接信息：

```properties
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=loadup_test
MYSQL_USER=root
MYSQL_PASSWORD=root
```

3. **启动 MySQL 容器**

```bash
docker-compose up -d
```

或者使用 VSCode DevContainer 插件自动启动。

### 运行测试

```bash
# 运行所有测试
mvn clean test

# 运行单个测试类
mvn test -Dtest=UserRepositoryTest

# 运行集成测试
mvn verify
```

## 测试配置

### 数据库配置

测试使用 MySQL 数据库，配置文件位于：

- `src/test/resources/application-test.yml` - Spring Boot 测试配置
- `src/test/resources/schema-test.sql` - 数据库表结构
- `src/test/resources/test-data.sql` - 测试数据

### HikariCP 连接池配置

连接池配置已在 `application-test.yml` 中设置：

```yaml
spring:
  datasource:
    hikari:
      pool-name: LoadupTestPool
      minimum-idle: 5
      maximum-pool-size: 20
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
├── io.github.loadup.modules.upms/
│   ├── UpmsTestApplication.java          # 测试应用入口
│   └── repository/                       # Repository 测试
│       ├── BaseRepositoryTest.java       # 基础测试类
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

### 数据库连接失败

如果遇到数据库连接失败，请检查：

1. MySQL 容器是否正常运行：
   ```bash
   docker ps
   ```

2. 环境变量是否正确设置

3. 数据库是否已创建：
   ```bash
   docker exec -it mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS loadup_test"
   ```

### 端口冲突

如果 3306 端口被占用，修改 `.env` 文件中的 `MYSQL_PORT` 和 `application-test.yml` 中的端口配置。

## 注意事项

1. 测试使用 `@Transactional` 注解，每个测试方法执行后自动回滚
2. 测试数据在每次测试运行前会重新初始化
3. 不要在测试中依赖数据的持久化状态

