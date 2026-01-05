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
package com.github.loadup.components.testcontainers.cache;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Redis TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton Redis container that starts once and is shared across all test
 * classes that use it. This significantly reduces test execution time by avoiding the overhead of
 * starting a new Redis container for each test class.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * &#64;TestPropertySource(properties = {
 *     "spring.redis.host=" + SharedRedisContainer.HOST,
 *     "spring.redis.port=" + SharedRedisContainer.PORT
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
public class SharedRedisContainer {

  /** Default Redis version to use */
  public static final String DEFAULT_REDIS_VERSION = "redis:7-alpine";

  /** Redis default port */
  public static final int REDIS_PORT = 6379;

  /** The shared Redis container instance */
  private static final GenericContainer<?> REDIS_CONTAINER;

  /** Redis host for the shared container */
  public static final String HOST;

  /** Redis port for the shared container */
  public static final Integer PORT;

  /** Redis connection URL */
  public static final String URL;

  static {
    // Read configuration from system properties or use defaults
    String redisVersion = System.getProperty("testcontainers.redis.version", DEFAULT_REDIS_VERSION);

    log.info("Initializing shared Redis TestContainer with version: {}", redisVersion);

    REDIS_CONTAINER =
        new GenericContainer<>(DockerImageName.parse(redisVersion))
            .withExposedPorts(REDIS_PORT)
            .withReuse(true);

    REDIS_CONTAINER.start();

    HOST = REDIS_CONTAINER.getHost();
    PORT = REDIS_CONTAINER.getMappedPort(REDIS_PORT);
    URL = "redis://" + HOST + ":" + PORT;

    log.info("Shared Redis TestContainer started successfully");
    log.info("Redis Host: {}", HOST);
    log.info("Redis Port: {}", PORT);
    log.info("Redis URL: {}", URL);

    // Add shutdown hook to stop the container when JVM exits
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Stopping shared Redis TestContainer");
                  REDIS_CONTAINER.stop();
                }));
  }

  /**
   * Get the shared Redis container instance. This method triggers the static initialization if not
   * already done.
   *
   * @return the shared Redis container instance
   */
  public static GenericContainer<?> getInstance() {
    return REDIS_CONTAINER;
  }

  /**
   * Get the Redis host.
   *
   * @return the host
   */
  public static String getHost() {
    return HOST;
  }

  /**
   * Get the Redis port.
   *
   * @return the port
   */
  public static Integer getPort() {
    return PORT;
  }

  /**
   * Get the Redis connection URL.
   *
   * @return the connection URL
   */
  public static String getUrl() {
    return URL;
  }

  /** Private constructor to prevent instantiation */
  private SharedRedisContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
