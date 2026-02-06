
package io.github.loadup.components.testcontainers;

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

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.components.testcontainers.database.AbstractPostgreSQLContainerTest;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Integration test class for SharedPostgreSQLContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(
        properties = {"loadup.testcontainers.enabled=true", "loadup.testcontainers.postgresql.enabled=true"})
class SharedPostgreSQLContainerIT extends AbstractPostgreSQLContainerTest {

    @Test
    void testContainerIsRunning() {
        PostgreSQLContainer container = SharedPostgreSQLContainer.getInstance();
        assertNotNull(container, "Container should not be null");
        assertTrue(container.isRunning(), "Container should be running");
    }

    @Test
    void testContainerProperties() {
        assertNotNull(SharedPostgreSQLContainer.getJdbcUrl(), "JDBC URL should not be null");
        assertNotNull(SharedPostgreSQLContainer.getUsername(), "Username should not be null");
        assertNotNull(SharedPostgreSQLContainer.getPassword(), "Password should not be null");
        assertNotNull(SharedPostgreSQLContainer.getDatabaseName(), "Database name should not be null");
        assertNotNull(SharedPostgreSQLContainer.getDriverClassName(), "Driver class name should not be null");
        assertNotNull(SharedPostgreSQLContainer.getHost(), "Host should not be null");
        assertNotNull(SharedPostgreSQLContainer.getMappedPort(), "Mapped port should not be null");

        log.info("JDBC URL: {}", SharedPostgreSQLContainer.getJdbcUrl());
        log.info("Username: {}", SharedPostgreSQLContainer.getUsername());
        log.info("Database: {}", SharedPostgreSQLContainer.getDatabaseName());
        log.info("Host: {}", SharedPostgreSQLContainer.getHost());
        log.info("Port: {}", SharedPostgreSQLContainer.getMappedPort());
    }

    @Test
    void testDatabaseConnection() throws Exception {
        String jdbcUrl = SharedPostgreSQLContainer.getJdbcUrl();
        String username = SharedPostgreSQLContainer.getUsername();
        String password = SharedPostgreSQLContainer.getPassword();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");

            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT 1");
                assertTrue(resultSet.next(), "Result set should have at least one row");
                assertEquals(1, resultSet.getInt(1), "Query result should be 1");
            }
        }
    }

    @Test
    void testCreateTable() throws Exception {
        String jdbcUrl = SharedPostgreSQLContainer.getJdbcUrl();
        String username = SharedPostgreSQLContainer.getUsername();
        String password = SharedPostgreSQLContainer.getPassword();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            try (Statement statement = connection.createStatement()) {
                // Create a test table
                statement.execute("CREATE TABLE IF NOT EXISTS test_table_pg (id INT PRIMARY KEY, name VARCHAR(100))");

                // Insert a record
                statement.execute(
                        "INSERT INTO test_table_pg (id, name) VALUES (1, 'test') ON CONFLICT (id) DO UPDATE SET name='test'");

                // Query the record
                ResultSet resultSet = statement.executeQuery("SELECT * FROM test_table_pg WHERE id = 1");
                assertTrue(resultSet.next(), "Should find the inserted record");
                assertEquals(1, resultSet.getInt("id"), "ID should be 1");
                assertEquals("test", resultSet.getString("name"), "Name should be 'test'");

                // Clean up
                statement.execute("DROP TABLE test_table_pg");
            }
        }
    }

    @Test
    void testSameContainerAcrossTests() {
        @SuppressWarnings("rawtypes")
        PostgreSQLContainer container1 = SharedPostgreSQLContainer.getInstance();
        @SuppressWarnings("rawtypes")
        PostgreSQLContainer container2 = SharedPostgreSQLContainer.getInstance();

        assertSame(container1, container2, "Should return the same container instance");
    }
}
