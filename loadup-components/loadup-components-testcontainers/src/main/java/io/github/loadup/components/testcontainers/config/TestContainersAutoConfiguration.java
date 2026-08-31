package io.github.loadup.components.testcontainers.config;

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
