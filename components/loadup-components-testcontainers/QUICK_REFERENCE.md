# LoadUp TestContainers 快速参考

## 一、添加依赖

```xml
<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

## 二、三种使用方式

### 方式 1: 继承抽象基类（最简单）✅

```java

@SpringBootTest
class MyTest extends AbstractMySQLContainerTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testSomething() {
        // 测试代码
    }
}
```

### 方式 2: 使用初始化器

```java

@SpringBootTest
@ContextConfiguration(initializers = MySQLContainerInitializer.class)
class MyTest {
    // 测试代码
}
```

### 方式 3: 直接使用（纯 JDBC）

```java
class MyTest {
    @Test
    void testJdbc() throws Exception {
        String url = SharedMySQLContainer.getJdbcUrl();
        try (Connection conn = DriverManager.getConnection(
                url,
                SharedMySQLContainer.getUsername(),
                SharedMySQLContainer.getPassword())) {
            // 测试代码
        }
    }
}
```

## 三、配置选项

### 系统属性配置

```bash
-Dtestcontainers.mysql.version=mysql:8.0.33
-Dtestcontainers.mysql.database=mydb
-Dtestcontainers.mysql.username=myuser
-Dtestcontainers.mysql.password=mypass
```

### Maven 配置

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <testcontainers.mysql.version>mysql:8.0.33</testcontainers.mysql.version>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

## 四、API 参考

### SharedMySQLContainer 静态方法

| 方法                     | 返回类型                | 说明       |
|------------------------|---------------------|----------|
| `getInstance()`        | `MySQLContainer<?>` | 获取容器实例   |
| `getJdbcUrl()`         | `String`            | JDBC URL |
| `getUsername()`        | `String`            | 用户名      |
| `getPassword()`        | `String`            | 密码       |
| `getDatabaseName()`    | `String`            | 数据库名     |
| `getDriverClassName()` | `String`            | 驱动类名     |
| `getHost()`            | `String`            | 主机地址     |
| `getMappedPort()`      | `Integer`           | 映射端口     |

### 公共常量

| 常量                      | 默认值                        | 说明       |
|-------------------------|----------------------------|----------|
| `DEFAULT_MYSQL_VERSION` | `mysql:8.0`                | MySQL 版本 |
| `DEFAULT_DATABASE_NAME` | `testdb`                   | 数据库名     |
| `DEFAULT_USERNAME`      | `test`                     | 用户名      |
| `DEFAULT_PASSWORD`      | `test`                     | 密码       |
| `DRIVER_CLASS_NAME`     | `com.mysql.cj.jdbc.Driver` | 驱动类      |

## 五、常见使用模式

### JPA Repository 测试

```java

@DataJpaTest
class UserRepositoryTest extends AbstractMySQLContainerTest {
    @Autowired
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void testSave() {
        User user = new User("test");
        User saved = repository.save(user);
        assertNotNull(saved.getId());
    }
}
```

### Service 层测试

```java

@SpringBootTest
@Transactional
class UserServiceTest extends AbstractMySQLContainerTest {
    @Autowired
    private UserService service;

    @Test
    void testCreateUser() {
        UserDTO dto = new UserDTO("test");
        UserDTO created = service.create(dto);
        assertNotNull(created.getId());
    }
}
```

### Liquibase 测试

```java

@SpringBootTest
class MigrationTest extends AbstractMySQLContainerTest {
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void testTableExists() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = ? AND table_name = ?",
                Integer.class, getDatabaseName(), "users");
        assertTrue(count > 0);
    }
}
```

## 六、性能优化

### 启用容器复用

在 `~/.testcontainers.properties`:

```properties
testcontainers.reuse.enable=true
```

### 提前拉取镜像

```bash
docker pull mysql:8.0
```

### 并行测试

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

## 七、故障排查

### Docker 未运行

```
错误: Could not find a valid Docker environment
解决: 启动 Docker Desktop
```

### 镜像下载慢

```
解决: 配置 Docker 镜像加速器
```

### 测试超时

```
解决: 增加超时时间或检查网络连接
```

## 八、完整示例

```java
package com.example.myapp;

import com.github.loadup.components.testcontainers.cloud.AbstractMySQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CompleteExampleTest extends AbstractMySQLContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 创建测试表
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS test_users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50), " +
                        "email VARCHAR(100))"
        );
    }

    @Test
    void testInsertAndQuery() {
        // 插入数据
        jdbcTemplate.update(
                "INSERT INTO test_users (username, email) VALUES (?, ?)",
                "testuser", "test@example.com"
        );

        // 查询数据
        String username = jdbcTemplate.queryForObject(
                "SELECT username FROM test_users WHERE email = ?",
                String.class,
                "test@example.com"
        );

        assertEquals("testuser", username);
    }

    @Test
    void testConnectionInfo() {
        // 验证容器信息
        assertNotNull(getJdbcUrl());
        assertNotNull(getUsername());
        assertNotNull(getPassword());

        System.out.println("JDBC URL: " + getJdbcUrl());
        System.out.println("Database: " + getDatabaseName());
    }
}
```

## 九、资源链接

- **项目文档**: `README.md`
- **详细示例**: `USAGE_EXAMPLES.md`
- **配置指南**: `CONFIGURATION_EXAMPLES.md`
- **实现总结**: `IMPLEMENTATION_SUMMARY.md`

## 十、下一步

1. 阅读完整文档了解更多功能
2. 在你的测试中集成 TestContainers
3. 配置 CI/CD 环境支持
4. 探索高级配置选项

---

**提示**: 推荐使用方式 1（继承抽象基类），这是最简单且最常用的方式！

