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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public final class SharedRedisContainer {
    private static final Logger log = LoggerFactory.getLogger(SharedRedisContainer.class);

    /**
     * Default Redis version to use
     */
    public static final String DEFAULT_REDIS_VERSION = "redis:7-alpine";

    /**
     * Redis default port
     */
    public static final int REDIS_PORT = 6379;

    /**
     * The shared Redis container instance
     */
    private static GenericContainer<?> redisContainer;

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * Redis host for the shared container
     */
    private static String host = "localhost";

    /**
     * Redis port for the shared container
     */
    private static Integer port = REDIS_PORT;

    /**
     * Redis connection URL
     */
    private static String url;

    public static void startContainer(ContainerConfig config) {
        if (STARTED.get()) {
            return;
        }
        synchronized (SharedRedisContainer.class) {
            if (STARTED.get()) {
                return;
            }
            String image = (config.getImage() != null) ? config.getImage() : DEFAULT_REDIS_VERSION;
            redisContainer = new GenericContainer<>(DockerImageName.parse(image))
                    .withExposedPorts(REDIS_PORT)
                    .withReuse(config.isReusable());
            redisContainer.start();
            STARTED.set(true);
            host = redisContainer.getHost();
            port = redisContainer.getMappedPort(REDIS_PORT);
            url = "redis://" + host + ":" + port;

            if (config.isReusable()) {
                log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
            } else {
                log.info("Reuse is disabled. Registering shutdown hook to stop container.");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (redisContainer != null) {
                        log.info("🛑 Stopping Redis TestContainer...");
                        redisContainer.stop();
                    }
                }));
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

        return redisContainer;
    }

    /**
     * Get the Redis host.
     *
     * @return the host
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getHost() {

        return host;
    }

    /**
     * Get the Redis port.
     *
     * @return the port
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static Integer getPort() {
        return port;
    }

    /**
     * Get the mapped port .
     *
     * @return the mapped port
     */
    public static Integer getMappedPort() {
        return redisContainer.getMappedPort(REDIS_PORT);
    }

    /**
     * Get the Redis connection URL.
     *
     * @return the connection URL
     */
    public static String getUrl() {
        return url;
    }

    /**
     * Private constructor to prevent instantiation
     */
    private SharedRedisContainer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isStarted() {
        return STARTED.get();
    }

    public static Map<String, String> getProperties() {
        return Map.of(
                "spring.data.redis.host", getHost(),
                "spring.data.redis.port", getMappedPort().toString());
    }
}
