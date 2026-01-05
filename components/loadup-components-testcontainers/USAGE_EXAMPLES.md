# TestContainers 使用示例

## Spring Boot 集成测试示例

### 示例 1: 基本数据库测试

```java
package com.example.myapp;

import com.github.loadup.components.testcontainers.cloud.AbstractMySQLContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseIntegrationTest extends AbstractMySQLContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseConnection() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, result);
    }

    @Test
    void testCreateAndQuery() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY, name VARCHAR(100))");
        jdbcTemplate.update("INSERT INTO users (id, name) VALUES (?, ?)", 1, "John Doe");

        String name = jdbcTemplate.queryForObject(
                "SELECT name FROM users WHERE id = ?",
                String.class,
                1
        );

        assertEquals("John Doe", name);

        jdbcTemplate.execute("DROP TABLE users");
    }
}
```

### 示例 2: JPA Repository 测试

```java
package com.example.myapp;

import com.github.loadup.components.testcontainers.cloud.AbstractMySQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest extends AbstractMySQLContainerTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testSaveAndFind() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());

        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    void testFindByUsername() {
        User user = new User();
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        userRepository.save(user);

        User found = userRepository.findByUsername("johndoe").orElse(null);
        assertNotNull(found);
        assertEquals("john@example.com", found.getEmail());
    }
}
```

### 示例 3: Service 层测试

```java
package com.example.myapp;

import com.github.loadup.components.testcontainers.cloud.AbstractMySQLContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest extends AbstractMySQLContainerTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setEmail("new@example.com");

        UserDTO created = userService.createUser(userDTO);

        assertNotNull(created.getId());
        assertEquals("newuser", created.getUsername());
    }

    @Test
    void testUpdateUser() {
        // Create user first
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("oldname");
        userDTO.setEmail("old@example.com");
        UserDTO created = userService.createUser(userDTO);

        // Update user
        created.setUsername("newname");
        UserDTO updated = userService.updateUser(created.getId(), created);

        assertEquals("newname", updated.getUsername());
        assertEquals("old@example.com", updated.getEmail());
    }
}
```

### 示例 4: 多数据源配置测试

```java
package com.example.myapp;

import com.github.loadup.components.testcontainers.cloud.MySQLContainerInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ContextConfiguration(initializers = MySQLContainerInitializer.class)
@TestPropertySource(properties = {
        "spring.datasource.secondary.url=" + SharedMySQLContainer.JDBC_URL + "?allowPublicKeyRetrieval=true",
        "spring.datasource.secondary.username=" + SharedMySQLContainer.USERNAME,
        "spring.datasource.secondary.password=" + SharedMySQLContainer.PASSWORD
})
class MultiDatasourceTest {

    @Autowired
    @Qualifier("primaryJdbcTemplate")
    private JdbcTemplate primaryJdbcTemplate;

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    private JdbcTemplate secondaryJdbcTemplate;

    @Test
    void testBothDatasources() {
        Integer result1 = primaryJdbcTemplate.queryForObject("SELECT 1", Integer.class);
        Integer result2 = secondaryJdbcTemplate.queryForObject("SELECT 1", Integer.class);

        assertEquals(1, result1);
        assertEquals(1, result2);
    }
}
```

### 示例 5: Liquibase 集成测试

```java
package com.example.myapp;

import com.github.loadup.components.testcontainers.cloud.AbstractMySQLContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class LiquibaseIntegrationTest extends AbstractMySQLContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testLiquibaseChangelog() {
        // 验证 Liquibase 创建的表
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                Integer.class,
                getDatabaseName(),
                "users"
        );

        assertTrue(count > 0, "Liquibase should have created the users table");
    }
}
```

## Maven 配置示例

### pom.xml 依赖配置

```xml
<dependencies>
    <!-- 测试依赖 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 自定义 Maven 配置

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
                <!-- 自定义 TestContainers 配置 -->
                <systemPropertyVariables>
                    <testcontainers.mysql.version>mysql:8.0.33</testcontainers.mysql.version>
                    <testcontainers.mysql.database>testdb</testcontainers.mysql.database>
                </systemPropertyVariables>
                
                <!-- 排除集成测试 -->
                <excludes>
                    <exclude>**/*IT.java</exclude>
                </excludes>
            </configuration>
        </plugin>
        
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <configuration>
                <!-- 运行集成测试 -->
                <includes>
                    <include>**/*IT.java</include>
                </includes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## application.yml 测试配置示例

```yaml
spring:
  datasource:
    # 这些配置会被 TestContainers 自动覆盖
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        
# Liquibase 配置
liquibase:
  enabled: true
  change-log: classpath:db/changelog/db.changelog-master.xml

# 日志配置
logging:
  level:
    org.testcontainers: INFO
    com.github.loadup: DEBUG
    org.hibernate.SQL: DEBUG
```

