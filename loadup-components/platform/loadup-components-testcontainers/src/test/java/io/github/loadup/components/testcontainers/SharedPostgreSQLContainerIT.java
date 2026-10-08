package io.github.loadup.components.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.testcontainers.database.AbstractPostgreSQLContainerTest;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Integration test class for SharedPostgreSQLContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
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

        LogUtil.info(SharedPostgreSQLContainerIT.class, "JDBC URL: {}", SharedPostgreSQLContainer.getJdbcUrl());
        LogUtil.info(SharedPostgreSQLContainerIT.class, "Username: {}", SharedPostgreSQLContainer.getUsername());
        LogUtil.info(SharedPostgreSQLContainerIT.class, "Database: {}", SharedPostgreSQLContainer.getDatabaseName());
        LogUtil.info(SharedPostgreSQLContainerIT.class, "Host: {}", SharedPostgreSQLContainer.getHost());
        LogUtil.info(SharedPostgreSQLContainerIT.class, "Port: {}", SharedPostgreSQLContainer.getMappedPort());
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
