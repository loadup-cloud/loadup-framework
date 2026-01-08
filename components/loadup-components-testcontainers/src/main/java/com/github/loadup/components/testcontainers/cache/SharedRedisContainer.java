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

import com.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
  private static final AtomicReference<GenericContainer<?>> REDIS_CONTAINER =
      new AtomicReference<>();

  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  /** Redis host for the shared container */
  public static final AtomicReference<String> HOST = new AtomicReference<>();

  /** Redis port for the shared container */
  public static final AtomicReference<Integer> PORT = new AtomicReference<>();

  /** Redis connection URL */
  public static final AtomicReference<String> URL = new AtomicReference<>();

  public static void startContainer(ContainerConfig config) {
    if (STARTED.get()) return;
    synchronized (SharedRedisContainer.class) {
      if (STARTED.get()) return;
      String image = (config.getVersion() != null) ? config.getVersion() : DEFAULT_REDIS_VERSION;
      REDIS_CONTAINER.set(
          new GenericContainer<>(DockerImageName.parse(image))
              .withExposedPorts(REDIS_PORT)
              .withReuse(config.isReuse()));
      REDIS_CONTAINER.get().start();
      STARTED.set(true);
      HOST.set(REDIS_CONTAINER.get().getHost());
      PORT.set(REDIS_CONTAINER.get().getMappedPort(REDIS_PORT));
      URL.set("redis://" + HOST + ":" + PORT);

      if (!config.isReuse()) {
        log.info("Reuse is disabled. Registering shutdown hook to stop container.");
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(
                    () -> {
                      if (REDIS_CONTAINER.get() != null) {
                        log.info("🛑 Stopping Redis TestContainer...");
                        REDIS_CONTAINER.get().stop();
                      }
                    }));
      } else {
        log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
      }
    }
  }

  /**
   * Get the shared Redis container instance. This method triggers the static initialization if not
   * already done.
   *
   * @return the shared Redis container instance
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static GenericContainer<?> getInstance() {
    checkStarted();

    return REDIS_CONTAINER.get();
  }

  /**
   * Get the Redis host.
   *
   * @return the host
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getHost() {
    checkStarted();

    return HOST.get();
  }

  /**
   * Get the Redis port.
   *
   * @return the port
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static Integer getPort() {
    checkStarted();
    return PORT.get();
  }

  /**
   * Get the mapped port .
   *
   * @return the mapped port
   */
  public static Integer getMappedPort() {
    return REDIS_CONTAINER.get().getMappedPort(REDIS_PORT);
  }

  /**
   * Get the Redis connection URL.
   *
   * @return the connection URL
   */
  public static String getUrl() {
    return URL.get();
  }

  /** Private constructor to prevent instantiation */
  private SharedRedisContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static boolean isStarted() {
    return STARTED.get();
  }

  private static void checkStarted() {
    if (!STARTED.get()) {
      throw new IllegalStateException("Redis Container has not been started yet!");
    }
  }
}
