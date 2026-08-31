package io.github.loadup.components.testcontainers.messaging;

/*-
 * #%L
 * Loadup Components TestContainers
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.kafka.KafkaContainer;
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
public final class SharedKafkaContainer {
    private static final Logger log = LoggerFactory.getLogger(SharedKafkaContainer.class);

    /**
     * Default Kafka version to use
     */
    public static final String DEFAULT_KAFKA_VERSION = "apache/kafka:4.1.1";

    /**
     * The shared Kafka container instance
     */
    private static KafkaContainer kafkaContainer;

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * Kafka bootstrap servers
     */
    private static String bootstrapServers;

    public static void startContainer(ContainerConfig config) {
        if (STARTED.get()) {
            return;
        }

        synchronized (SharedKafkaContainer.class) {
            if (STARTED.get()) {
                return;
            }

            String imageName = (config.getImage() != null) ? config.getImage() : DEFAULT_KAFKA_VERSION;

            log.info("🚀 Starting Shared Kafka TestContainer: {}", imageName);

            kafkaContainer = new KafkaContainer(DockerImageName.parse(imageName)).withReuse(config.isReusable());

            kafkaContainer.start();
            STARTED.set(true);

            bootstrapServers = kafkaContainer.getBootstrapServers();

            log.info("✅ Kafka Container started at: {}", kafkaContainer.getBootstrapServers());

            // Register shutdown hook if reuse is disabled
            if (config.isReusable()) {
                log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
            } else {
                log.info("Reuse is disabled. Registering shutdown hook to stop container.");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (kafkaContainer != null) {
                        log.info("🛑 Stopping Kafka TestContainer...");
                        kafkaContainer.stop();
                    }
                }));
            }
        }
    }

    /**
     * Get the shared Kafka container instance.
     *
     * @return the shared Kafka container instance
     * @throws IllegalStateException if TestContainers is not started
     */
    public static KafkaContainer getInstance() {
        checkStarted();
        return kafkaContainer;
    }

    /**
     * Get the Kafka bootstrap servers.
     *
     * @return the bootstrap servers
     * @throws IllegalStateException if TestContainers is not started
     */
    public static String getBootstrapServers() {
        checkStarted();
        return bootstrapServers;
    }

    /**
     * Get the Kafka host.
     *
     * @return the host
     * @throws IllegalStateException if TestContainers is not started
     */
    public static String getHost() {
        checkStarted();
        return kafkaContainer.getHost();
    }

    /**
     * Get the Kafka port.
     *
     * @return the port
     * @throws IllegalStateException if TestContainers is not started
     */
    public static Integer getPort() {
        checkStarted();
        return kafkaContainer.getFirstMappedPort();
    }

    /**
     * Get all container properties as a Map for Spring environment injection.
     *
     * @return map of property names to values
     */
    public static java.util.Map<String, String> getProperties() {
        return java.util.Map.of("spring.kafka.bootstrap-servers", getBootstrapServers());
    }

    private SharedKafkaContainer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isStarted() {
        return STARTED.get();
    }

    private static void checkStarted() {
        if (!STARTED.get()) {
            throw new IllegalStateException("MongoDB Container has not been started yet!");
        }
    }
}
