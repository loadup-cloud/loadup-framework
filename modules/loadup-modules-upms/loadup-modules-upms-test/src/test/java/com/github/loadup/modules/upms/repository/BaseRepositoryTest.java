package com.github.loadup.modules.upms.repository;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base Repository Test Configuration
 *
 * <p>Provides common configuration for all repository tests: - Spring Boot Test context - H2
 * in-memory database - MyBatis-Flex mapper scanning - Transaction rollback after each test - Test
 * profile activation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = com.github.loadup.modules.upms.UpmsTestApplication.class,
    properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@MapperScan("com.github.loadup.modules.upms.infrastructure.mapper")
@ComponentScan(
    basePackages = {
      "com.github.loadup.modules.upms.infrastructure.repository",
      "com.github.loadup.modules.upms.infrastructure.converter",
      "com.github.loadup.components.database"
    })
@ActiveProfiles("test")
@Transactional
public abstract class BaseRepositoryTest {

  /** Common test utilities and helper methods can be added here */

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
