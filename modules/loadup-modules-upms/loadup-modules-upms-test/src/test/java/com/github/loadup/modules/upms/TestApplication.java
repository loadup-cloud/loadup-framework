package com.github.loadup.modules.upms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * Test Application for UPMS Module Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootApplication
// @Import(MyBatisTestConfig.class)
@ComponentScan(
    basePackages = {
      "com.github.loadup.modules.upms.infrastructure.repository",
      "com.github.loadup.modules.upms.infrastructure.converter",
      "com.github.loadup.components.database"
    })
@MapperScan("com.github.loadup.modules.upms.infrastructure.mapper")
public class TestApplication {
  @Container
  static MySQLContainer<?> mySQLContainer =
      new MySQLContainer<>("mysql:8.0")
          .withDatabaseName("test")
          .withUsername("root")
          .withPassword("123456")
          .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")
          .withReuse(true);

  static {
    mySQLContainer.start();

    // 配置JVM级别的系统属性，确保所有测试类使用同一个容器
    System.setProperty("spring.datasource.url", mySQLContainer.getJdbcUrl());
    System.setProperty("spring.datasource.username", mySQLContainer.getUsername());
    System.setProperty("spring.datasource.password", mySQLContainer.getPassword());
    System.setProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
  }

  // @DynamicPropertySource
  // static void configureProperties(DynamicPropertyRegistry registry) {
  //  System.out.println("JDBC URL: " + mySQLContainer.getJdbcUrl());
  //  registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
  //  registry.add("spring.datasource.username", mySQLContainer::getUsername);
  //  registry.add("spring.datasource.password", mySQLContainer::getPassword);
  //  registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
  // }

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }
}
