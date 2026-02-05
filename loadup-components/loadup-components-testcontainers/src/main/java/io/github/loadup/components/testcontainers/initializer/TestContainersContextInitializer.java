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
package io.github.loadup.components.testcontainers.initializer;

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
import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import io.github.loadup.components.testcontainers.database.SharedMySQLContainer;
import io.github.loadup.components.testcontainers.database.SharedMongoDBContainer;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import io.github.loadup.components.testcontainers.messaging.SharedKafkaContainer;
import io.github.loadup.components.testcontainers.search.SharedElasticsearchContainer;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInfo;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Initializer for testcontainers using LoadUp Components Testcontainers.
 *
 * <p>Supports two initialization modes:
 * <ol>
 *   <li>Annotation-based: Use @EnableTestContainers annotation on test class</li>
 *   <li>Configuration-based: Configure via application properties</li>
 * </ol>
 *
 * <p>Configuration example:
 * <pre>
 * loadup:
 *   testcontainers:
 *     enabled: true
 *     reusable: true
 *     mysql:
 *       enabled: true
 *       image: mysql:8.0
 *       database: testdb
 * </pre>
 *
 * <p>Annotation example:
 * <pre>
 * &#64;SpringBootTest
 * &#64;EnableTestContainers(ContainerType.MYSQL, ContainerType.REDIS)
 * class MyIntegrationTest {
 *     // Your test code here
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
public class TestContainersContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /** Thread-local to store test class for accessing from initializer */
    private static final ThreadLocal<Class<?>> TEST_CLASS = new ThreadLocal<>();

    /**
     * Set the test class. Should be called before context initialization.
     * This is automatically handled by @EnableTestContainers annotation.
     */
    public static void setTestClass(Class<?> testClass) {
        TEST_CLASS.set(testClass);
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment env = context.getEnvironment();

        log.info(">>> [TESTCONTAINERS] ========== Initializer Started ==========");

        // 1. Try to get test class from thread-local first (set by extension)
        Class<?> testClass = TEST_CLASS.get();

        // 2. If not set, try to find it from Spring Boot configuration classes
        if (testClass == null) {
            log.info(">>> [TESTCONTAINERS] Test class not in ThreadLocal, searching from Spring Boot context...");
            testClass = findTestClassFromSpringContext(context);
        }

        // 3. Fallback: try stack trace (legacy)
        if (testClass == null) {
            log.info(">>> [TESTCONTAINERS] Not found in Spring context, trying stack trace...");
            testClass = findTestClassFromStackTrace();
        }

        if (testClass != null) {
            log.info(">>> [TESTCONTAINERS] Found test class: {}", testClass.getName());
        } else {
            log.warn(">>> [TESTCONTAINERS] No test class found!");
        }

        // 3. Check annotation-based configuration
        Set<ContainerType> annotatedContainers = getAnnotatedContainers(testClass);
        boolean reuseFromAnnotation = getReuseFromAnnotation(testClass);

        if (!annotatedContainers.isEmpty()) {
            log.info(">>> [TESTCONTAINERS] Found @EnableTestContainers with containers: {}", annotatedContainers);
        }

        // 4. Check property-based configuration
        TestContainersProperties properties =
                Binder.get(env)
                        .bind("loadup.testcontainers", TestContainersProperties.class)
                        .orElse(null);

        // 5. Determine which containers to start
        Set<ContainerType> containersToStart = new HashSet<>();
        boolean reuse = reuseFromAnnotation;

        if (!annotatedContainers.isEmpty()) {
            // Annotation takes precedence
            containersToStart.addAll(annotatedContainers);
            log.info(">>> [TESTCONTAINERS] Using annotation-based container configuration");
        } else if (properties != null && properties.isEnabled()) {
            // Fall back to property-based configuration
            containersToStart.addAll(getEnabledContainersFromProperties(properties));
            reuse = properties.isReusable();
            log.info(">>> [TESTCONTAINERS] Using property-based container configuration");
        } else {
            log.debug(">>> [TESTCONTAINERS] No containers configured, skipping initialization");
            return;
        }

        if (containersToStart.isEmpty()) {
            log.debug(">>> [TESTCONTAINERS] No containers enabled, skipping initialization");
            return;
        }

        log.info(
                ">>> [TESTCONTAINERS] Starting containers: {} with reuse={}",
                containersToStart,
                reuse);

        // 6. Start containers and collect properties
        Map<String, String> containerProperties = new HashMap<>();

        for (ContainerType type : containersToStart) {
            try {
                ContainerConfig config = getContainerConfig(type, properties, reuse);
                Map<String, String> props = startContainer(type, config);
                if (props != null && !props.isEmpty()) {
                    containerProperties.putAll(props);
                    log.info(">>> [TESTCONTAINERS] Successfully started {} container", type);
                }
            } catch (Exception e) {
                log.error(
                        ">>> [TESTCONTAINERS] Failed to start {} container: {}",
                        type,
                        e.getMessage(),
                        e);
                throw new RuntimeException("Failed to start " + type + " container", e);
            }
        }

        // 7. Inject container properties into Spring environment
        if (!containerProperties.isEmpty()) {
            TestPropertyValues.of(containerProperties).applyTo(env);
            log.info(
                    ">>> [TESTCONTAINERS] Injected {} container properties into environment",
                    containerProperties.size());
        }

        // Clean up thread-local
        TEST_CLASS.remove();
    }

    /**
     * Get containers specified via @EnableTestContainers annotation.
     */
    private Set<ContainerType> getAnnotatedContainers(Class<?> testClass) {
        Set<ContainerType> containers = new HashSet<>();

        if (testClass != null) {
            EnableTestContainers annotation =
                    AnnotationUtils.findAnnotation(testClass, EnableTestContainers.class);
            if (annotation != null && annotation.value().length > 0) {
                containers.addAll(Arrays.asList(annotation.value()));
            }
        }

        return containers;
    }

    /**
     * Get reuse setting from @EnableTestContainers annotation.
     */
    private boolean getReuseFromAnnotation(Class<?> testClass) {
        if (testClass != null) {
            EnableTestContainers annotation =
                    AnnotationUtils.findAnnotation(testClass, EnableTestContainers.class);
            if (annotation != null) {
                return annotation.reuse();
            }
        }

        return true; // Default to reuse
    }

    /**
     * Find test class or application class with @EnableTestContainers annotation.
     *
     * <p>Strategy (in order of priority):
     * 1. Check Spring Boot Test context attributes (test class info)
     * 2. Check application.properties for spring.main.sources
     * 3. Scan classpath for @SpringBootApplication classes
     *
     * <p>This method works at ApplicationContextInitializer stage, before beans are initialized.
     */
    private Class<?> findTestClassFromSpringContext(ConfigurableApplicationContext context) {
        ConfigurableEnvironment env = context.getEnvironment();

        try {
            // Strategy 1: Try to get test class from Spring Boot Test context
            // Spring Boot Test stores test class info in various ways

            // Check org.springframework.boot.test.context.SpringBootTestContextBootstrapper
            String testClass = env.getProperty("spring.test.context.test-class");
            if (testClass != null) {
                log.debug(">>> [TESTCONTAINERS] Found test class property: {}", testClass);
                try {
                    Class<?> clazz = Class.forName(testClass);
                    EnableTestContainers annotation = AnnotationUtils.findAnnotation(clazz, EnableTestContainers.class);
                    if (annotation != null) {
                        log.info(">>> [TESTCONTAINERS] Found @EnableTestContainers on test class: {}", testClass);
                        return clazz;
                    }
                } catch (ClassNotFoundException e) {
                    log.debug(">>> [TESTCONTAINERS] Cannot load test class: {}", testClass);
                }
            }

            // Strategy 2: Check spring.main.sources (typically @SpringBootApplication class)
            String mainSources = env.getProperty("spring.main.sources");
            if (mainSources != null) {
                log.debug(">>> [TESTCONTAINERS] Found spring.main.sources: {}", mainSources);
                try {
                    Class<?> clazz = Class.forName(mainSources);
                    EnableTestContainers annotation = AnnotationUtils.findAnnotation(clazz, EnableTestContainers.class);
                    if (annotation != null) {
                        log.info(">>> [TESTCONTAINERS] Found @EnableTestContainers on main source: {}", mainSources);
                        return clazz;
                    }
                } catch (ClassNotFoundException e) {
                    log.debug(">>> [TESTCONTAINERS] Cannot load main source: {}", mainSources);
                }
            }

            // Strategy 3: Check @SpringBootConfiguration from context id
            // Spring Boot Test context id usually contains the test class name
            String contextId = context.getId();
            if (contextId != null) {
                log.debug(">>> [TESTCONTAINERS] Context ID: {}", contextId);

                // Context ID format: "TestContext@1234567 [testClass = com.example.MyTest, ...]"
                int testClassIndex = contextId.indexOf("testClass = ");
                if (testClassIndex > 0) {
                    int startIndex = testClassIndex + "testClass = ".length();
                    int endIndex = contextId.indexOf(",", startIndex);
                    if (endIndex < 0) {
                        endIndex = contextId.indexOf("]", startIndex);
                    }

                    if (endIndex > startIndex) {
                        String className = contextId.substring(startIndex, endIndex).trim();
                        log.debug(">>> [TESTCONTAINERS] Extracted test class from context ID: {}", className);
                        try {
                            Class<?> clazz = Class.forName(className);
                            EnableTestContainers annotation = AnnotationUtils.findAnnotation(clazz, EnableTestContainers.class);
                            if (annotation != null) {
                                log.info(">>> [TESTCONTAINERS] Found @EnableTestContainers on: {}", className);
                                return clazz;
                            }
                        } catch (ClassNotFoundException e) {
                            log.debug(">>> [TESTCONTAINERS] Cannot load class from context ID: {}", className);
                        }
                    }
                }
            }

            // Strategy 4: Scan common application class patterns
            // Try to find @SpringBootApplication class in common packages
            String[] commonPackages = {
                "io.github.loadup.testify.test",
                "io.github.loadup.testify.demo",
                "io.github.loadup.testify",
                "io.github.loadup"
            };

            for (String packageName : commonPackages) {
                String[] candidateNames = {
                    packageName + ".Application",
                    packageName + ".TestApplication",
                    packageName + ".DemoApplication"
                };

                for (String candidateName : candidateNames) {
                    try {
                        Class<?> clazz = Class.forName(candidateName);
                        EnableTestContainers annotation = AnnotationUtils.findAnnotation(clazz, EnableTestContainers.class);
                        if (annotation != null) {
                            log.info(">>> [TESTCONTAINERS] Found @EnableTestContainers on: {}", candidateName);
                            return clazz;
                        }
                    } catch (ClassNotFoundException e) {
                        // Try next candidate
                    }
                }
            }

            log.debug(">>> [TESTCONTAINERS] No @EnableTestContainers annotation found in Spring context");

        } catch (Exception e) {
            log.warn(">>> [TESTCONTAINERS] Error scanning Spring context: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Find test class from stack trace (fallback method).
     * This is a legacy fallback when Spring context scanning doesn't find the annotation.
     */
    private Class<?> findTestClassFromStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        log.debug(">>> [TESTCONTAINERS] Fallback: scanning stack trace");

        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();

            // Skip framework classes
            if (className.startsWith("org.springframework")
                || className.startsWith("org.testng")
                || className.startsWith("org.junit")
                || className.startsWith("java.")
                || className.startsWith("jdk.internal")) {
                continue;
            }

            try {
                Class<?> clazz = Class.forName(className);

                // Check for @EnableTestContainers annotation
                EnableTestContainers annotation =
                    AnnotationUtils.findAnnotation(clazz, EnableTestContainers.class);

                if (annotation != null) {
                    log.info(">>> [TESTCONTAINERS] Found @EnableTestContainers in stack trace: {}",
                            className);
                    return clazz;
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                // Ignore
            }
        }

        return null;
    }

    /**
     * Get enabled containers from properties.
     */
    private Set<ContainerType> getEnabledContainersFromProperties(
            TestContainersProperties properties) {
        Set<ContainerType> containers = new HashSet<>();

        if (properties.getMysql() != null && properties.getMysql().isEnabled()) {
            containers.add(ContainerType.MYSQL);
        }
        if (properties.getPostgresql() != null && properties.getPostgresql().isEnabled()) {
            containers.add(ContainerType.POSTGRESQL);
        }
        if (properties.getMongodb() != null && properties.getMongodb().isEnabled()) {
            containers.add(ContainerType.MONGODB);
        }
        if (properties.getKafka() != null && properties.getKafka().isEnabled()) {
            containers.add(ContainerType.KAFKA);
        }
        if (properties.getElasticsearch() != null && properties.getElasticsearch().isEnabled()) {
            containers.add(ContainerType.ELASTICSEARCH);
        }

        return containers;
    }

    /**
     * Get container configuration.
     */
    private ContainerConfig getContainerConfig(
            ContainerType type, TestContainersProperties properties, boolean reuse) {
        ContainerConfig config = new ContainerConfig();
        config.setEnabled(true);
        config.setReusable(reuse);

        // Get type-specific configuration from properties if available
        if (properties != null) {
            ContainerConfig typeConfig =
                    switch (type) {
                        case MYSQL -> properties.getMysql();
                        case POSTGRESQL -> properties.getPostgresql();
                        case MONGODB -> properties.getMongodb();
                        case REDIS -> properties.getRedis();
                        case KAFKA -> properties.getKafka();
                        case ELASTICSEARCH -> properties.getElasticsearch();
                        case LOCALSTACK -> properties.getLocalstack();
                    };

            if (typeConfig != null && typeConfig.isEnabled()) {
                return typeConfig;
            }
        }

        return config;
    }

    /**
     * Start a container based on its type.
     */
    private Map<String, String> startContainer(ContainerType type, ContainerConfig config) {
        return switch (type) {
            case MYSQL -> {
                SharedMySQLContainer.startContainer(config);
                yield Map.of(
                        "spring.datasource.url",
                        SharedMySQLContainer.getJdbcUrl(),
                        "spring.datasource.username",
                        SharedMySQLContainer.getUsername(),
                        "spring.datasource.password",
                        SharedMySQLContainer.getPassword(),
                        "spring.datasource.driver-class-name",
                        SharedMySQLContainer.getDriverClassName());
            }
            case POSTGRESQL -> {
                SharedPostgreSQLContainer.startContainer(config);
                yield Map.of(
                        "spring.datasource.url",
                        SharedPostgreSQLContainer.getJdbcUrl(),
                        "spring.datasource.username",
                        SharedPostgreSQLContainer.getUsername(),
                        "spring.datasource.password",
                        SharedPostgreSQLContainer.getPassword(),
                        "spring.datasource.driver-class-name",
                        SharedPostgreSQLContainer.getDriverClassName());
            }
            case MONGODB -> {
                SharedMongoDBContainer.startContainer(config);
                yield Map.of(
                        "spring.data.mongodb.uri", SharedMongoDBContainer.getConnectionString());
            }
            case REDIS -> {
                // Redis container needs to be implemented in components-testcontainers
                log.warn(">>> [TESTCONTAINERS] Redis container not yet implemented in Shared containers");
                yield Map.of();
            }
            case KAFKA -> {
                SharedKafkaContainer.startContainer(config);
                yield Map.of(
                        "spring.kafka.bootstrap-servers", SharedKafkaContainer.getBootstrapServers());
            }
            case ELASTICSEARCH -> {
                SharedElasticsearchContainer.startContainer(config);
                yield Map.of(
                        "spring.elasticsearch.uris",
                        SharedElasticsearchContainer.getHttpHostAddress());
            }
            case LOCALSTACK -> {
                // LocalStack container requires additional configuration
                log.warn(">>> [TESTCONTAINERS] LocalStack container requires manual configuration");
                yield Map.of();
            }
        };
    }
}
