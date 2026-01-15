/*
 * Copyright (c) 2026 LoadUp Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.loadup.components.testcontainers.database;

import com.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared MySQL TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton MySQL container that starts once and is shared across all test
 * classes that use it. This significantly reduces test execution time by avoiding the overhead of
 * starting a new MySQL container for each test class.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * &#64;TestPropertySource(properties = {
 *     "spring.datasource.url=" + SharedMySQLContainer.JDBC_URL,
 *     "spring.datasource.username=" + SharedMySQLContainer.USERNAME,
 *     "spring.datasource.password=" + SharedMySQLContainer.PASSWORD
 * })
 * class MyIntegrationTest {
 *     // Your test code here
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class SharedMySQLContainer {

  /** Default MySQL version to use */
  public static final String DEFAULT_MYSQL_VERSION = "mysql:8.0";

  /** Default database name */
  private static final String DEFAULT_DATABASE_NAME = "testdb";

  /** Default username */
  private static final String DEFAULT_USERNAME = "test";

  /** Default password */
  private static final String DEFAULT_PASSWORD = "test";

  /** The shared MySQL container instance */
  private static MySQLContainer MYSQL_CONTAINER;

  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  /** JDBC URL for the shared MySQL container */
  private static String JDBC_URL;

  /** Username for the shared MySQL container */
  private static String USERNAME;

  /** Password for the shared MySQL container */
  private static String PASSWORD;

  /** Database name for the shared MySQL container */
  private static String DATABASE_NAME;

  /** JDBC driver class name */
  public static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

  /**
   * 初始化并启动容器
   *
   * @param config 来自 Spring 环境变量的配置对象
   */
  public static void startContainer(ContainerConfig config) {
    if (STARTED.get()) {
      return;
    }

    synchronized (SharedMySQLContainer.class) {
      if (STARTED.get()) {
        return;
      }

      String imageName =
          (config != null && config.getVersion() != null)
              ? config.getVersion()
              : DEFAULT_MYSQL_VERSION;

      log.info("🚀 Starting Shared MySQL TestContainer: {}", imageName);

      MYSQL_CONTAINER =
          new MySQLContainer(DockerImageName.parse(imageName))
              .withDatabaseName(getValue(config.getDatabase(), DEFAULT_DATABASE_NAME))
              .withUsername(getValue(config.getUsername(), DEFAULT_USERNAME))
              .withPassword(getValue(config.getPassword(), DEFAULT_PASSWORD))
              // 优化：增加重用和性能参数
              .withCommand("--max-allowed-packet=268435456")
              .withReuse(config.isReuse()); // 1. 应用复用配置

      MYSQL_CONTAINER.start();
      STARTED.set(true);
      JDBC_URL = (MYSQL_CONTAINER.getJdbcUrl());
      USERNAME = (MYSQL_CONTAINER.getUsername());
      PASSWORD = (MYSQL_CONTAINER.getPassword());
      DATABASE_NAME = (MYSQL_CONTAINER.getDatabaseName());

      log.info("✅ MySQL Container started at: {}", MYSQL_CONTAINER.getJdbcUrl());

      // JVM 退出时自动关闭
      // 2. 智能关闭钩子
      if (!config.isReuse()) {
        log.info("Reuse is disabled. Registering shutdown hook to stop container.");
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(
                    () -> {
                      if (MYSQL_CONTAINER != null) {
                        log.info("🛑 Stopping MySQL TestContainer...");
                        MYSQL_CONTAINER.stop();
                      }
                    }));
      } else {
        log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
      }
    }
  }

  private static String getValue(String value, String defaultValue) {
    return (value == null || value.isEmpty()) ? defaultValue : value;
  }

  /**
   * Get the shared MySQL container instance. This method triggers the static initialization if not
   * already done.
   *
   * @return the shared MySQL container instance
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static MySQLContainer getInstance() {
    return MYSQL_CONTAINER;
  }

  /**
   * Get the JDBC URL for the shared MySQL container.
   *
   * @return the JDBC URL
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getJdbcUrl() {
    checkStarted();
    return JDBC_URL;
  }

  /**
   * Get the username for the shared MySQL container.
   *
   * @return the username
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getUsername() {
    checkStarted();
    return USERNAME;
  }

  /**
   * Get the password for the shared MySQL container.
   *
   * @return the password
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getPassword() {
    checkStarted();
    return PASSWORD;
  }

  /**
   * Get the database name for the shared MySQL container.
   *
   * @return the database name
   */
  public static String getDatabaseName() {
    return DATABASE_NAME;
  }

  /**
   * Get the JDBC driver class name.
   *
   * @return the driver class name
   */
  public static String getDriverClassName() {
    return DRIVER_CLASS_NAME;
  }

  /**
   * Get the mapped port for MySQL.
   *
   * @return the mapped port
   */
  public static Integer getMappedPort() {
    return MYSQL_CONTAINER.getMappedPort(MySQLContainer.MYSQL_PORT);
  }

  /**
   * Get the host for the MySQL container.
   *
   * @return the host
   */
  public static String getHost() {
    return MYSQL_CONTAINER.getHost();
  }

  /** Private constructor to prevent instantiation */
  private SharedMySQLContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static boolean isStarted() {
    return STARTED.get();
  }

  private static void checkStarted() {
    if (!STARTED.get()) {
      throw new IllegalStateException("MySQL Container has not been started yet!");
    }
  }
}
