package io.github.loadup.components.testcontainers.search;

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
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public final class SharedElasticsearchContainer {
    private static final Logger log = LoggerFactory.getLogger(SharedElasticsearchContainer.class);

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
