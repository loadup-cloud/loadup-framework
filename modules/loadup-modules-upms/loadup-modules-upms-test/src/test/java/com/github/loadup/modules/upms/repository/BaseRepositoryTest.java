package com.github.loadup.modules.upms.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base Repository Test Configuration
 *
 * <p>Provides common configuration for all repository tests:
 *
 * <ul>
 *   <li>Spring Boot Test context
 *   <li>MySQL database with Testcontainers (auto-managed container)
 *   <li>HikariCP connection pool
 *   <li>MyBatis-Flex mapper scanning
 *   <li>Transaction rollback after each test
 *   <li>Test profile activation
 * </ul>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@MapperScan("com.github.loadup.modules.upms.infrastructure.mapper")
@ComponentScan(
    basePackages = {
      "com.github.loadup.modules.upms.infrastructure.repository",
      "com.github.loadup.modules.upms.infrastructure.converter",
      "com.github.loadup.components.database"
    })
@ActiveProfiles("test")
@Transactional
@Testcontainers
public abstract class BaseRepositoryTest {

  /** MySQL Testcontainer - shared across all tests */
  @Container
  protected static final MySQLContainer<?> mysqlContainer =
      new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
          .withCommand("--default-authentication-plugin=mysql_native_password") // 添加这一行
          .withDatabaseName("loadup_test")
          .withUsername("test")
          .withPassword("test")
          .withReuse(false);

  @BeforeAll
  static void beforeAll() {
    // Check Docker availability
    System.out.println("=== Testcontainers Docker Check ===");
    try {
      // Test if Docker is accessible
      String dockerHost = System.getenv("DOCKER_HOST");
      System.out.println("DOCKER_HOST: " + dockerHost);

      // Check if OrbStack socket exists
      java.io.File orbstackSocket =
          new java.io.File(System.getProperty("user.home") + "/.orbstack/run/docker.sock");
      System.out.println("OrbStack socket exists: " + orbstackSocket.exists());

      // Check if standard socket exists
      java.io.File standardSocket = new java.io.File("/var/run/docker.sock");
      System.out.println("Standard socket exists: " + standardSocket.exists());

      System.out.println("=== End Docker Check ===");
    } catch (Exception e) {
      System.err.println("Error checking Docker: " + e.getMessage());
    }
  }

  /** Configure datasource properties dynamically from Testcontainer */
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.datasource.url",
        () ->
            mysqlContainer.getJdbcUrl()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    registry.add("spring.datasource.username", mysqlContainer::getUsername);
    registry.add("spring.datasource.password", mysqlContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
  }

  /** Generate a unique test ID */
  protected String generateTestId() {
    return "TEST_" + System.currentTimeMillis();
  }

  /** Sleep for a short duration (useful for timestamp-based tests) */
  protected void shortSleep() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
