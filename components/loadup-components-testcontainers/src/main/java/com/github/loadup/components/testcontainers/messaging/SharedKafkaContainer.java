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
package com.github.loadup.components.testcontainers.messaging;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Kafka TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton Kafka container that starts once and is shared across all test
 * classes that use it.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class SharedKafkaContainer {

  /** Default Kafka version to use */
  public static final String DEFAULT_KAFKA_VERSION = "confluentinc/cp-kafka:7.5.0";

  /** The shared Kafka container instance */
  private static final KafkaContainer KAFKA_CONTAINER;

  /** Kafka bootstrap servers */
  public static final String BOOTSTRAP_SERVERS;

  static {
    // Read configuration from system properties or use defaults
    String kafkaVersion = System.getProperty("testcontainers.kafka.version", DEFAULT_KAFKA_VERSION);

    log.info("Initializing shared Kafka TestContainer with version: {}", kafkaVersion);

    KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse(kafkaVersion)).withReuse(true);

    KAFKA_CONTAINER.start();

    BOOTSTRAP_SERVERS = KAFKA_CONTAINER.getBootstrapServers();

    log.info("Shared Kafka TestContainer started successfully");
    log.info("Bootstrap Servers: {}", BOOTSTRAP_SERVERS);

    // Add shutdown hook
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Stopping shared Kafka TestContainer");
                  KAFKA_CONTAINER.stop();
                }));
  }

  public static KafkaContainer getInstance() {
    return KAFKA_CONTAINER;
  }

  public static String getBootstrapServers() {
    return BOOTSTRAP_SERVERS;
  }

  public static String getHost() {
    return KAFKA_CONTAINER.getHost();
  }

  public static Integer getPort() {
    return KAFKA_CONTAINER.getFirstMappedPort();
  }

  private SharedKafkaContainer() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
