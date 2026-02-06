
package io.github.loadup.components.testcontainers.config;

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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

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
public class TestContainersAutoConfiguration {

    // This is intentionally empty.
    // TestExecutionListener is registered via spring.factories
    // Properties are enabled via @EnableConfigurationProperties
}
