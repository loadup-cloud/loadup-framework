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
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared PostgreSQL TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton PostgreSQL container that starts once and is shared across all
 * test classes that use it. This significantly reduces test execution time by avoiding the overhead
 * of starting a new PostgreSQL container for each test class.
 *
 * <p>Usage example:
 *
 * <pre>
 * String jdbcUrl = SharedPostgreSQLContainer.getJdbcUrl();
 * String username = SharedPostgreSQLContainer.getUsername();
 * String password = SharedPostgreSQLContainer.getPassword();
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class SharedPostgreSQLContainer {

  /** Default PostgreSQL version to use */
  public static final String DEFAULT_POSTGRES_VERSION = "postgres:15-alpine";

  /** Default database name */
  public static final String DEFAULT_DATABASE_NAME = "testdb";

  /** Default username */
  public static final String DEFAULT_USERNAME = "test";

  /** Default password */
  public static final String DEFAULT_PASSWORD = "test";

  /** JDBC driver class name */
  public static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";

  /** The shared PostgreSQL container instance */
  private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

  /** PostgreSQL JDBC URL for the shared container */
  public static final String JDBC_URL;

  /** PostgreSQL username for the shared container */
  public static final String USERNAME;

  /** PostgreSQL password for the shared container */
  public static final String PASSWORD;

  /** PostgreSQL database name */
  public static final String DATABASE_NAME;

  /** PostgreSQL host */
  public static final String HOST;

  /** PostgreSQL port */
  public static final Integer PORT;

  static {
    // Read configuration from system properties or use defaults
    String postgresVersion =
        System.getProperty("testcontainers.postgres.version", DEFAULT_POSTGRES_VERSION);
    String databaseName =
        System.getProperty("testcontainers.postgres.database", DEFAULT_DATABASE_NAME);
    String username = System.getProperty("testcontainers.postgres.username", DEFAULT_USERNAME);
    String password = System.getProperty("testcontainers.postgres.password", DEFAULT_PASSWORD);

    log.info("Initializing shared PostgreSQL TestContainer with version: {}", postgresVersion);

    POSTGRES_CONTAINER =
        new PostgreSQLContainer<>(postgresVersion)
            .withDatabaseName(databaseName)
            .withUsername(username)
            .withPassword(password)
            .withReuse(true);

    POSTGRES_CONTAINER.start();

    JDBC_URL = POSTGRES_CONTAINER.getJdbcUrl();
    USERNAME = POSTGRES_CONTAINER.getUsername();
    PASSWORD = POSTGRES_CONTAINER.getPassword();
    DATABASE_NAME = POSTGRES_CONTAINER.getDatabaseName();
    HOST = POSTGRES_CONTAINER.getHost();
    PORT = POSTGRES_CONTAINER.getFirstMappedPort();

    log.info("Shared PostgreSQL TestContainer started successfully");
    log.info("JDBC URL: {}", JDBC_URL);
    log.info("Username: {}", USERNAME);
    log.info("Database: {}", DATABASE_NAME);

    // Add shutdown hook to stop the container when JVM exits
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Stopping shared PostgreSQL TestContainer");
                  POSTGRES_CONTAINER.stop();
                }));
  }

  /**
   * Get the shared PostgreSQL container instance. This method triggers the static initialization if
   * not already done.
   *
   * @return the shared PostgreSQL container instance
   */
  public static PostgreSQLContainer<?> getInstance() {
    return POSTGRES_CONTAINER;
  }

  /**
   * Get the PostgreSQL JDBC URL.
   *
   * @return the JDBC URL
   */
  public static String getJdbcUrl() {
    return JDBC_URL;
  }

  /**
   * Get the PostgreSQL username.
   *
   * @return the username
   */
  public static String getUsername() {
    return USERNAME;
  }

  /**
   * Get the PostgreSQL password.
   *
   * @return the password
   */
  public static String getPassword() {
    return PASSWORD;
  }

  /**
   * Get the PostgreSQL database name.
   *
   * @return the database name
   */
  public static String getDatabaseName() {
    return DATABASE_NAME;
  }

  /**
   * Get the PostgreSQL JDBC driver class name.
   *
   * @return the driver class name
   */
  public static String getDriverClassName() {
    return DRIVER_CLASS_NAME;
  }

  /**
   * Get the PostgreSQL host.
   *
   * @return the host
   */
  public static String getHost() {
    return HOST;
  }

  /**
   * Get the PostgreSQL mapped port.
   *
   * @return the mapped port
   */
  public static Integer getMappedPort() {
    return PORT;
  }

  /** Private constructor to prevent instantiation */
  private SharedPostgreSQLContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
