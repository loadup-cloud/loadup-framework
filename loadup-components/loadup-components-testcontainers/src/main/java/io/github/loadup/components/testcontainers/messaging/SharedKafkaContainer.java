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
package io.github.loadup.components.testcontainers.messaging;

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
@Slf4j
public class SharedKafkaContainer {

    /** Default Kafka version to use */
    public static final String DEFAULT_KAFKA_VERSION = "apache/kafka:4.1.1";

    /** The shared Kafka container instance */
    private static KafkaContainer KAFKA_CONTAINER;

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /** Kafka bootstrap servers */
    private static String BOOTSTRAP_SERVERS;

    public static void startContainer(ContainerConfig config) {
        if (STARTED.get()) {
            return;
        }

        synchronized (SharedKafkaContainer.class) {
            if (STARTED.get()) {
                return;
            }

            String imageName = (config.getVersion() != null) ? config.getVersion() : DEFAULT_KAFKA_VERSION;

            log.info("🚀 Starting Shared Kafka TestContainer: {}", imageName);

            KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse(imageName)).withReuse(config.isReuse());

            KAFKA_CONTAINER.start();
            STARTED.set(true);

            BOOTSTRAP_SERVERS = KAFKA_CONTAINER.getBootstrapServers();

            log.info("✅ Kafka Container started at: {}", KAFKA_CONTAINER.getBootstrapServers());

            // JVM 退出时自动关闭
            // 2. 智能关闭钩子
            if (!config.isReuse()) {
                log.info("Reuse is disabled. Registering shutdown hook to stop container.");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (KAFKA_CONTAINER != null) {
                        log.info("🛑 Stopping Kafka TestContainer...");
                        KAFKA_CONTAINER.stop();
                    }
                }));
            } else {
                log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
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
        return KAFKA_CONTAINER;
    }

    /**
     * Get the Kafka bootstrap servers.
     *
     * @return the bootstrap servers
     * @throws IllegalStateException if TestContainers is not started
     */
    public static String getBootstrapServers() {
        checkStarted();
        return BOOTSTRAP_SERVERS;
    }

    /**
     * Get the Kafka host.
     *
     * @return the host
     * @throws IllegalStateException if TestContainers is not started
     */
    public static String getHost() {
        checkStarted();
        return KAFKA_CONTAINER.getHost();
    }

    /**
     * Get the Kafka port.
     *
     * @return the port
     * @throws IllegalStateException if TestContainers is not started
     */
    public static Integer getPort() {
        checkStarted();
        return KAFKA_CONTAINER.getFirstMappedPort();
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
