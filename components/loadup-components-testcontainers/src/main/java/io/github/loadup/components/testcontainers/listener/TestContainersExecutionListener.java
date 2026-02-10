package io.github.loadup.components.testcontainers.listener;

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

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import io.github.loadup.components.testcontainers.database.SharedMongoDBContainer;
import io.github.loadup.components.testcontainers.database.SharedMySQLContainer;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import io.github.loadup.components.testcontainers.messaging.SharedKafkaContainer;
import io.github.loadup.components.testcontainers.search.SharedElasticsearchContainer;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * TestExecutionListener that starts testcontainers based on @EnableTestContainers annotation.
 *
 * <p>This listener runs before the Spring ApplicationContext is initialized, allowing it to
 * start containers and inject their properties into the test environment.
 *
 * <p>Usage:
 * <pre>
 * &#64;SpringBootTest
 * &#64;EnableTestContainers(ContainerType.MYSQL)
 * class MyIntegrationTest {
 *     // MySQL container automatically started
 * }
 * </pre>
 *
 * <p>The listener is automatically registered via spring.factories and runs with highest precedence
 * to ensure containers start before any beans are initialized.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class TestContainersExecutionListener extends AbstractTestExecutionListener {

    /**
     * Thread-local to store container properties before ApplicationContext is created.
     */
    private static final ThreadLocal<Map<String, String>> CONTAINER_PROPERTIES = new ThreadLocal<>();

    /**
     * Thread-local to track which test classes have been processed to avoid duplicate initialization.
     */
    private static final ThreadLocal<Class<?>> PROCESSED_TEST_CLASS = new ThreadLocal<>();

    @Override
    public int getOrder() {
        // Highest precedence to ensure containers start before any other listeners
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        Class<?> testClass = testContext.getTestClass();

        // Check if already processed to avoid duplicate initialization
        if (PROCESSED_TEST_CLASS.get() == testClass) {
            log.debug(">>> [TESTCONTAINERS] Test class {} already processed, skipping", testClass.getSimpleName());
            return;
        }

        log.info(">>> [TESTCONTAINERS] ========== TestExecutionListener Started ==========");
        log.info(">>> [TESTCONTAINERS] Processing test class: {}", testClass.getName());

        // Find @EnableTestContainers annotation
        EnableTestContainers annotation = AnnotationUtils.findAnnotation(testClass, EnableTestContainers.class);

        if (annotation == null) {
            log.debug(
                    ">>> [TESTCONTAINERS] No @EnableTestContainers annotation found on {}", testClass.getSimpleName());
            return;
        }

        ContainerType[] containerTypes = annotation.value();
        boolean reuse = annotation.reuse();

        if (containerTypes == null || containerTypes.length == 0) {
            log.warn(">>> [TESTCONTAINERS] @EnableTestContainers annotation found but no containers specified");
            return;
        }

        log.info(">>> [TESTCONTAINERS] Found @EnableTestContainers with containers: {}", (Object) containerTypes);
        log.info(">>> [TESTCONTAINERS] Container reuse enabled: {}", reuse);

        // Start containers and collect properties (but don't inject yet - ApplicationContext not ready)
        Map<String, String> allProperties = new HashMap<>();

        for (ContainerType type : containerTypes) {
            try {
                Map<String, String> containerProperties = startContainer(type, reuse);

                if (!containerProperties.isEmpty()) {
                    allProperties.putAll(containerProperties);
                    log.info(
                            ">>> [TESTCONTAINERS] Successfully started {} container, collected {} properties",
                            type,
                            containerProperties.size());
                } else {
                    log.warn(">>> [TESTCONTAINERS] Container {} started but no properties collected", type);
                }

            } catch (Exception e) {
                log.error(">>> [TESTCONTAINERS] Failed to start {} container: {}", type, e.getMessage(), e);
                throw new RuntimeException(
                        "Failed to start " + type + " container for test class " + testClass.getName(), e);
            }
        }

        // Store properties in ThreadLocal for later injection in prepareTestInstance
        if (!allProperties.isEmpty()) {
            CONTAINER_PROPERTIES.set(allProperties);
            log.info(">>> [TESTCONTAINERS] Stored {} properties for later injection", allProperties.size());
        }

        // Mark as processed
        PROCESSED_TEST_CLASS.set(testClass);

        log.info(">>> [TESTCONTAINERS] ========== Container Startup Complete ==========");
    }

    /**
     * Get stored container properties (for use by ApplicationContextInitializer).
     *
     * @return map of container properties, or null if no properties stored
     */
    public static Map<String, String> getStoredProperties() {
        return CONTAINER_PROPERTIES.get();
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        // Clean up thread-local
        PROCESSED_TEST_CLASS.remove();
        CONTAINER_PROPERTIES.remove();
    }

    /**
     * Start a specific container type and return its connection properties.
     *
     * @param type the container type to start
     * @param reuse whether to reuse existing containers
     * @return map of property names to values for Spring environment injection
     */
    private Map<String, String> startContainer(ContainerType type, boolean reuse) {
        ContainerConfig config = new ContainerConfig();
        config.setEnabled(true);
        config.setReusable(reuse);

        return switch (type) {
            case MYSQL -> {
                log.info(">>> [TESTCONTAINERS] Starting MySQL container...");
                SharedMySQLContainer.startContainer(config);
                yield SharedMySQLContainer.getProperties();
            }
            case POSTGRESQL -> {
                log.info(">>> [TESTCONTAINERS] Starting PostgreSQL container...");
                SharedPostgreSQLContainer.startContainer(config);
                yield SharedPostgreSQLContainer.getProperties();
            }
            case MONGODB -> {
                log.info(">>> [TESTCONTAINERS] Starting MongoDB container...");
                SharedMongoDBContainer.startContainer(config);
                yield SharedMongoDBContainer.getProperties();
            }
            case REDIS -> {
                log.warn(">>> [TESTCONTAINERS] Redis container not yet fully implemented");
                yield Map.of();
            }
            case KAFKA -> {
                log.info(">>> [TESTCONTAINERS] Starting Kafka container...");
                SharedKafkaContainer.startContainer(config);
                yield SharedKafkaContainer.getProperties();
            }
            case ELASTICSEARCH -> {
                log.info(">>> [TESTCONTAINERS] Starting Elasticsearch container...");
                SharedElasticsearchContainer.startContainer(config);
                yield SharedElasticsearchContainer.getProperties();
            }
            case LOCALSTACK -> {
                log.warn(">>> [TESTCONTAINERS] LocalStack container requires additional configuration");
                yield Map.of();
            }
        };
    }
}
