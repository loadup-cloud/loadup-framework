package io.github.loadup.components.testcontainers.database;

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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared MongoDB TestContainer instance that can be reused across multiple tests.
 *
 * <p>This class provides a singleton MongoDB container that starts once and is shared across all
 * test classes that use it.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public final class SharedMongoDBContainer {
    private static final Logger log = LoggerFactory.getLogger(SharedMongoDBContainer.class);

    /**
     * Default MongoDB version to use
     */
    public static final String DEFAULT_MONGODB_VERSION = "mongo:7.0";

    /**
     * Default replica set name
     */
    private static final String DEFAULT_DATABASE_NAME = "testdb";

    /**
     * The shared MongoDB container instance
     */
    private static MongoDBContainer mongodbContainer;

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * MongoDB connection string
     */
    private static String connectionString;

    /**
     * MongoDB host
     */
    private static String host;

    /**
     * MongoDB port
     */
    private static Integer port;

    /**
     * MongoDB replica set URL
     */
    private static String replicaSetUrl;

    public static void startContainer(ContainerConfig config) {
        if (STARTED.get()) {
            return;
        }

        synchronized (SharedMongoDBContainer.class) {
            if (STARTED.get()) {
                return;
            }

            String imageName = (config.getImage() != null) ? config.getImage() : DEFAULT_MONGODB_VERSION;

            log.info("🚀 Starting Shared MongoDB TestContainer: {}", imageName);

            mongodbContainer = new MongoDBContainer(DockerImageName.parse(imageName)).withReuse(config.isReusable());

            mongodbContainer.start();
            STARTED.set(true);

            connectionString = mongodbContainer.getConnectionString();
            host = mongodbContainer.getHost();
            port = mongodbContainer.getFirstMappedPort();
            replicaSetUrl = mongodbContainer.getReplicaSetUrl();

            log.info("✅ MongoDB Container started at: {}", mongodbContainer.getReplicaSetUrl());

            // Register shutdown hook if reuse is disabled
            if (config.isReusable()) {
                log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
            } else {
                log.info("Reuse is disabled. Registering shutdown hook to stop container.");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (mongodbContainer != null) {
                        log.info("🛑 Stopping MongoDB TestContainer...");
                        mongodbContainer.stop();
                    }
                }));
            }
        }
    }

    /**
     * Get the shared MongoDB container instance.
     *
     * @return the shared MongoDB container instance
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static MongoDBContainer getInstance() {
        return mongodbContainer;
    }

    /**
     * Get the MongoDB connection string.
     *
     * @return the connection string
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getConnectionString() {
        return connectionString;
    }

    /**
     * Get the MongoDB host.
     *
     * @return the host
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getHost() {
        return host;
    }

    /**
     * Get the MongoDB port.
     *
     * @return the port
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static Integer getMappedPort() {
        return port;
    }

    public static String getReplicaSetUrl() {
        return replicaSetUrl;
    }

    /**
     * Get all container properties as a Map for Spring environment injection.
     *
     * @return map of property names to values
     */
    public static Map<String, String> getProperties() {
        return Map.of(
                "spring.data.mongodb.uri",
                getConnectionString(),
                "spring.data.mongodb.database",
                DEFAULT_DATABASE_NAME,
                "spring.data.mongodb.host",
                getHost(),
                "spring.data.mongodb.port",
                getMappedPort().toString());
    }

    private SharedMongoDBContainer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isStarted() {
        return STARTED.get();
    }
}
