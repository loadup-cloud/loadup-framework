package io.github.loadup.components.testcontainers.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Auto-configuration for TestContainers integration.
 *
 * <p>This configuration is automatically loaded by Spring Boot 3's auto-configuration mechanism.
 * TestExecutionListener is still registered via spring.factories as it's part of Spring Test framework.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(TestContainersProperties.class)
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class TestContainersAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(TestContainersAutoConfiguration.class);

    // This is intentionally empty.
    // TestExecutionListener is registered via spring.factories
    // Properties are enabled via @EnableConfigurationProperties
}
