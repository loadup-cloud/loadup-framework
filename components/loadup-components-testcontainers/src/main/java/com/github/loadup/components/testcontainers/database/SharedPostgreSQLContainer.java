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
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

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

  /** Enable flag for TestContainers */
  private static AtomicBoolean STARTED = new AtomicBoolean(false);

  /** The shared PostgreSQL container instance */
  private static PostgreSQLContainer POSTGRES_CONTAINER;

  /** PostgreSQL JDBC URL for the shared container */
  public static String JDBC_URL;

  /** PostgreSQL username for the shared container */
  public static String USERNAME;

  /** PostgreSQL password for the shared container */
  public static String PASSWORD;

  /** PostgreSQL database name */
  public static String DATABASE_NAME;

  /** PostgreSQL host */
  public static String HOST;

  /** PostgreSQL port */
  public static Integer PORT;

  public static void startContainer(ContainerConfig config) {
    if (STARTED.get()) {
      return;
    }

    synchronized (SharedPostgreSQLContainer.class) {
      if (STARTED.get()) {
        return;
      }

      String imageName =
          (config.getVersion() != null) ? config.getVersion() : DEFAULT_POSTGRES_VERSION;

      log.info("🚀 Starting Shared PostgreSQL TestContainer: {}", imageName);

      POSTGRES_CONTAINER =
          new PostgreSQLContainer(DockerImageName.parse(imageName))
              .withDatabaseName(getValue(config.getDatabase(), DEFAULT_DATABASE_NAME))
              .withUsername(getValue(config.getUsername(), DEFAULT_USERNAME))
              .withPassword(getValue(config.getPassword(), DEFAULT_PASSWORD))
              // 允许重用
              .withReuse(config.isReuse());

      POSTGRES_CONTAINER.start();
      STARTED.set(true);

      JDBC_URL = POSTGRES_CONTAINER.getJdbcUrl();
      USERNAME = POSTGRES_CONTAINER.getUsername();
      PASSWORD = POSTGRES_CONTAINER.getPassword();
      DATABASE_NAME = POSTGRES_CONTAINER.getDatabaseName();
      HOST = POSTGRES_CONTAINER.getHost();
      PORT = POSTGRES_CONTAINER.getFirstMappedPort();
      log.info("✅ PostgreSQL Container started at: {}", POSTGRES_CONTAINER.getJdbcUrl());

      // JVM 退出时自动关闭
      // 2. 智能关闭钩子
      if (!config.isReuse()) {
        log.info("Reuse is disabled. Registering shutdown hook to stop container.");
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(
                    () -> {
                      if (POSTGRES_CONTAINER != null) {
                        log.info("🛑 Stopping PostgreSQL TestContainer...");
                        POSTGRES_CONTAINER.stop();
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
   * Get the shared PostgreSQL container instance. This method triggers the static initialization if
   * not already done.
   *
   * @return the shared PostgreSQL container instance
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static PostgreSQLContainer getInstance() {
    checkStarted();
    return POSTGRES_CONTAINER;
  }

  /**
   * Get the PostgreSQL JDBC URL.
   *
   * @return the JDBC URL
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getJdbcUrl() {
    checkStarted();
    return JDBC_URL;
  }

  /**
   * Get the PostgreSQL username.
   *
   * @return the username
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getUsername() {
    checkStarted();
    return USERNAME;
  }

  /**
   * Get the PostgreSQL password.
   *
   * @return the password
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getPassword() {
    checkStarted();
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

  public static boolean isStarted() {
    return STARTED.get();
  }

  private static void checkStarted() {
    if (!STARTED.get()) {
      throw new IllegalStateException("MySQL Container has not been started yet!");
    }
  }
}
