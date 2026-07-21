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
public final class SharedElasticsearchContainer {

    /**
     * Default Elasticsearch version to use
     */
    public static final String DEFAULT_ELASTICSEARCH_VERSION = "docker.elastic.co/elasticsearch/elasticsearch:8.11.0";

    /**
     * Enable flag for TestContainers
     */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * The shared Elasticsearch container instance
     */
    private static ElasticsearchContainer elasticsearchContainer;

    /**
     * Elasticsearch HTTP host URL
     */
    private static String httpHostAddress;

    /**
     * Elasticsearch host
     */
    private static String host;

    /**
     * Elasticsearch port
     */
    private static Integer port;

    public static void startContainer(ContainerConfig config) {
        if (STARTED.get()) {
            return;
        }

        synchronized (SharedElasticsearchContainer.class) {
            if (STARTED.get()) {
                return;
            }

            String imageName = (config.getImage() != null) ? config.getImage() : DEFAULT_ELASTICSEARCH_VERSION;

            log.info("🚀 Starting Shared Elasticsearch TestContainer: {}", imageName);

            elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse(imageName))
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("xpack.security.http.ssl.enabled", "false")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .withStartupTimeout(Duration.ofMinutes(3))
                    .waitingFor(Wait.forHttp("/").forStatusCode(200))
                    .withReuse(config.isReusable());

            elasticsearchContainer.start();
            STARTED.set(true);

            httpHostAddress = elasticsearchContainer.getHttpHostAddress();
            host = elasticsearchContainer.getHost();
            port = elasticsearchContainer.getFirstMappedPort();
            log.info("✅ Elasticsearch Container started at: {}", elasticsearchContainer.getHttpHostAddress());

            // Register shutdown hook if reuse is disabled
            if (config.isReusable()) {
                log.info("♻️ Reuse is enabled. Container will persist after JVM exits.");
            } else {
                log.info("Reuse is disabled. Registering shutdown hook to stop container.");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (elasticsearchContainer != null) {
                        log.info("🛑 Stopping Elasticsearch TestContainer...");
                        elasticsearchContainer.stop();
                    }
                }));
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
        return elasticsearchContainer;
    }

    /**
     * Get the Elasticsearch HTTP host address.
     *
     * @return the HTTP host address
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getHttpHostAddress() {
        checkStarted();
        return httpHostAddress;
    }

    /**
     * Get the Elasticsearch host.
     *
     * @return the host
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static String getHost() {
        checkStarted();
        return host;
    }

    /**
     * Get the Elasticsearch port.
     *
     * @return the port
     * @throws IllegalStateException if TestContainers is disabled
     */
    public static Integer getPort() {
        checkStarted();
        return port;
    }

    /**
     * Get all container properties as a Map for Spring environment injection.
     *
     * @return map of property names to values
     */
    public static java.util.Map<String, String> getProperties() {
        return java.util.Map.of("spring.elasticsearch.uris", getHttpHostAddress());
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
