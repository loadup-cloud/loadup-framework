package io.github.loadup.components.testcontainers.database;

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

/**
 * Abstract base test class that automatically configures MySQL TestContainer.
 *
 * <p>Test classes can extend this class to automatically use the shared MySQL container without
 * needing to manually configure the context initializer.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * class MyIntegrationTest extends AbstractMySQLContainerTest {
 *     &#64;Autowired
 *     private DataSource dataSource;
 *
 *     &#64;Test
 *     void testDatabaseConnection() {
 *         // Your test code here
 *     }
 * }
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@EnableTestContainers(ContainerType.MYSQL)
public abstract class AbstractMySQLContainerTest {

    /**
     * Get the JDBC URL for the shared MySQL container.
     *
     * @return the JDBC URL
     */
    protected String getJdbcUrl() {
        return SharedMySQLContainer.getJdbcUrl();
    }

    /**
     * Get the username for the shared MySQL container.
     *
     * @return the username
     */
    protected String getUsername() {
        return SharedMySQLContainer.getUsername();
    }

    /**
     * Get the password for the shared MySQL container.
     *
     * @return the password
     */
    protected String getPassword() {
        return SharedMySQLContainer.getPassword();
    }

    /**
     * Get the database name for the shared MySQL container.
     *
     * @return the database name
     */
    protected String getDatabaseName() {
        return SharedMySQLContainer.getDatabaseName();
    }
}
