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
package io.github.loadup.components.testcontainers.cache;

/*-
 * #%L
 * Loadup Components TestContainers
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.concurrent.atomic.AtomicBoolean;
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
  private static GenericContainer<?> REDIS_CONTAINER;

  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  /** Redis host for the shared container */
  private static String HOST = "localhost";

  /** Redis port for the shared container */
  private static Integer PORT = REDIS_PORT;

  /** Redis connection URL */
  private static String URL;

  public static void startContainer(ContainerConfig config) {
    if (STARTED.get()) return;
    synchronized (SharedRedisContainer.class) {
      if (STARTED.get()) return;
      String image = (config.getVersion() != null) ? config.getVersion() : DEFAULT_REDIS_VERSION;
      REDIS_CONTAINER =
          new GenericContainer<>(DockerImageName.parse(image))
              .withExposedPorts(REDIS_PORT)
              .withReuse(config.isReuse());
      REDIS_CONTAINER.start();
      STARTED.set(true);
      HOST = (REDIS_CONTAINER.getHost());
      PORT = (REDIS_CONTAINER.getMappedPort(REDIS_PORT));
      URL = ("redis://" + HOST + ":" + PORT);

      if (!config.isReuse()) {
        log.info("Reuse is disabled. Registering shutdown hook to stop container.");
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(
                    () -> {
                      if (REDIS_CONTAINER != null) {
                        log.info("🛑 Stopping Redis TestContainer...");
                        REDIS_CONTAINER.stop();
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

    return REDIS_CONTAINER;
  }

  /**
   * Get the Redis host.
   *
   * @return the host
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getHost() {

    return HOST;
  }

  /**
   * Get the Redis port.
   *
   * @return the port
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static Integer getPort() {
    return PORT;
  }

  /**
   * Get the mapped port .
   *
   * @return the mapped port
   */
  public static Integer getMappedPort() {
    return REDIS_CONTAINER.getMappedPort(REDIS_PORT);
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

  public static boolean isStarted() {
    return STARTED.get();
  }
}
