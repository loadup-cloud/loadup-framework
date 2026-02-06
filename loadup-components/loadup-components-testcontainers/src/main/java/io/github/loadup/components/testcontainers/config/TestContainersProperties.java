
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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for TestContainers.
 *
 * <p>This class provides centralized configuration for all TestContainers, allowing flexible
 * switching between TestContainers and real services.
 *
 * <p>Usage example in application.yml:
 *
 * <pre>
 * loadup:
 *   testcontainers:
 *     enabled: true
 *     mysql:
 *       enabled: true
 *       version: mysql:8.0
 *     redis:
 *       enabled: false  # Use real Redis
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "loadup.testcontainers")
public class TestContainersProperties {

    /** Global switch to enable/disable all TestContainers (default: true) */
    private boolean enabled = true;

    /** MySQL container configuration */
    private ContainerConfig mysql = new ContainerConfig();

    /** PostgreSQL container configuration */
    private ContainerConfig postgresql = new ContainerConfig();

    /** MongoDB container configuration */
    private ContainerConfig mongodb = new ContainerConfig();

    /** Redis container configuration */
    private ContainerConfig redis = new ContainerConfig();

    /** Kafka container configuration */
    private ContainerConfig kafka = new ContainerConfig();

    /** Elasticsearch container configuration */
    private ContainerConfig elasticsearch = new ContainerConfig();

    /** LocalStack container configuration */
    private ContainerConfig localstack = new ContainerConfig();
    private boolean reusable;

    /**
     * Configuration for a specific container type.
     *
     * @author LoadUp Framework
     * @since 1.0.0
     */
    @Data
    public static class ContainerConfig {
        /** Enable this specific container (default: false, must be explicitly enabled) */
        private boolean enabled = false;

        /** Docker image version (e.g., "mysql:8.0") */
        private String image;

        /** Database name (for database containers) */
        private String database;

        /** Username (for services requiring authentication) */
        private String username;

        /** Password (for services requiring authentication) */
        private String password;

        /** 开启复用，默认为 true。极大提升本地多次运行测试的速度。 */
        private boolean reusable = true;
    }
}
