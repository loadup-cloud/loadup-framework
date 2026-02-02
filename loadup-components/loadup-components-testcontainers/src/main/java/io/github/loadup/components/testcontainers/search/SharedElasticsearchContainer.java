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
package io.github.loadup.components.testcontainers.search;

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
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Elasticsearch TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton Elasticsearch container that starts once and is shared across
 * all test classes that use it.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class SharedElasticsearchContainer {

  /** Default Elasticsearch version to use */
  public static final String DEFAULT_ELASTICSEARCH_VERSION =
      "docker.elastic.co/elasticsearch/elasticsearch:8.11.0";

  /** Enable flag for TestContainers */
  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  /** The shared Elasticsearch container instance */
  private static ElasticsearchContainer ELASTICSEARCH_CONTAINER;

  /** Elasticsearch HTTP host URL */
  private static String HTTP_HOST_ADDRESS;

  /** Elasticsearch host */
  private static String HOST;

  /** Elasticsearch port */
  private static Integer PORT;

  public static void startContainer(ContainerConfig config) {
    if (STARTED.get()) {
      return;
    }

    synchronized (SharedElasticsearchContainer.class) {
      if (STARTED.get()) {
        return;
      }

      String imageName =
          (config.getVersion() != null) ? config.getVersion() : DEFAULT_ELASTICSEARCH_VERSION;

      log.info("🚀 Starting Shared Elasticsearch TestContainer: {}", imageName);

      ELASTICSEARCH_CONTAINER =
          new ElasticsearchContainer(DockerImageName.parse(imageName))
              // 关键配置：关闭 xpack 安全特性
              .withEnv("xpack.security.enabled", "false")
              .withEnv("xpack.security.http.ssl.enabled", "false")
              // 限制内存占用
              .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
              // 增加启动超时时间（ES 启动确实慢）
              .withStartupTimeout(Duration.ofMinutes(3))
              .waitingFor(Wait.forHttp("/").forStatusCode(200))
              .withReuse(config.isReuse());

      ELASTICSEARCH_CONTAINER.start();
      STARTED.set(true);

      HTTP_HOST_ADDRESS = ELASTICSEARCH_CONTAINER.getHttpHostAddress();
      HOST = ELASTICSEARCH_CONTAINER.getHost();
      PORT = ELASTICSEARCH_CONTAINER.getFirstMappedPort();
      log.info(
          "✅ Elasticsearch Container started at: {}", ELASTICSEARCH_CONTAINER.getHttpHostAddress());

      // JVM 退出时自动关闭
      // 2. 智能关闭钩子
      if (!config.isReuse()) {
        log.info("Reuse is disabled. Registering shutdown hook to stop container.");
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(
                    () -> {
                      if (ELASTICSEARCH_CONTAINER != null) {
                        log.info("🛑 Stopping Elasticsearch TestContainer...");
                        ELASTICSEARCH_CONTAINER.stop();
                      }
                    }));
      } else {
        log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
      }
    }
  }

  /**
   * Get the shared Elasticsearch container instance.
   *
   * @return the shared Elasticsearch container instance
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static ElasticsearchContainer getInstance() {
    checkStarted();
    return ELASTICSEARCH_CONTAINER;
  }

  /**
   * Get the Elasticsearch HTTP host address.
   *
   * @return the HTTP host address
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getHttpHostAddress() {
    checkStarted();
    return HTTP_HOST_ADDRESS;
  }

  /**
   * Get the Elasticsearch host.
   *
   * @return the host
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static String getHost() {
    checkStarted();
    return HOST;
  }

  /**
   * Get the Elasticsearch port.
   *
   * @return the port
   * @throws IllegalStateException if TestContainers is disabled
   */
  public static Integer getPort() {
    checkStarted();
    return PORT;
  }

  private SharedElasticsearchContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static boolean isStarted() {
    return STARTED.get();
  }

  private static void checkStarted() {
    if (!STARTED.get()) {
      throw new IllegalStateException("Elasticsearch Container has not been started yet!");
    }
  }
}
