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
  public static final String DEFAULT_DATABASE_NAME = "testdb";

  /** Default username */
  public static final String DEFAULT_USERNAME = "test";

  /** Default password */
  public static final String DEFAULT_PASSWORD = "test";

  /** The shared MySQL container instance */
  private static final MySQLContainer<?> MYSQL_CONTAINER;

  /** JDBC URL for the shared MySQL container */
  public static final String JDBC_URL;

  /** Username for the shared MySQL container */
  public static final String USERNAME;

  /** Password for the shared MySQL container */
  public static final String PASSWORD;

  /** Database name for the shared MySQL container */
  public static final String DATABASE_NAME;

  /** JDBC driver class name */
  public static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

  static {
    // Read configuration from system properties or use defaults
    String mysqlVersion = System.getProperty("testcontainers.mysql.version", DEFAULT_MYSQL_VERSION);
    String databaseName =
        System.getProperty("testcontainers.mysql.database", DEFAULT_DATABASE_NAME);
    String username = System.getProperty("testcontainers.mysql.username", DEFAULT_USERNAME);
    String password = System.getProperty("testcontainers.mysql.password", DEFAULT_PASSWORD);

    log.info("Initializing shared MySQL TestContainer with version: {}", mysqlVersion);

    MYSQL_CONTAINER =
        new MySQLContainer<>(DockerImageName.parse(mysqlVersion))
            .withDatabaseName(databaseName)
            .withUsername(username)
            .withPassword(password)
            .withCommand("--max-allowed-packet=268435456")
            .withReuse(true);

    MYSQL_CONTAINER.start();

    JDBC_URL = MYSQL_CONTAINER.getJdbcUrl();
    USERNAME = MYSQL_CONTAINER.getUsername();
    PASSWORD = MYSQL_CONTAINER.getPassword();
    DATABASE_NAME = MYSQL_CONTAINER.getDatabaseName();

    log.info("Shared MySQL TestContainer started successfully");
    log.info("JDBC URL: {}", JDBC_URL);
    log.info("Username: {}", USERNAME);
    log.info("Database: {}", DATABASE_NAME);

    // Add shutdown hook to stop the container when JVM exits
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Stopping shared MySQL TestContainer");
                  MYSQL_CONTAINER.stop();
                }));
  }

  /**
   * Get the shared MySQL container instance. This method triggers the static initialization if not
   * already done.
   *
   * @return the shared MySQL container instance
   */
  public static MySQLContainer<?> getInstance() {
    return MYSQL_CONTAINER;
  }

  /**
   * Get the JDBC URL for the shared MySQL container.
   *
   * @return the JDBC URL
   */
  public static String getJdbcUrl() {
    return JDBC_URL;
  }

  /**
   * Get the username for the shared MySQL container.
   *
   * @return the username
   */
  public static String getUsername() {
    return USERNAME;
  }

  /**
   * Get the password for the shared MySQL container.
   *
   * @return the password
   */
  public static String getPassword() {
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
}
