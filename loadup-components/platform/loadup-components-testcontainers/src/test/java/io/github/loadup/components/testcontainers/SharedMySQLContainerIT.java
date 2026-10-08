package io.github.loadup.components.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.testcontainers.database.AbstractMySQLContainerTest;
import io.github.loadup.components.testcontainers.database.SharedMySQLContainer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Integration test class for SharedMySQLContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {"loadup.testcontainers.enabled=true", "loadup.testcontainers.mysql.enabled=true"})
class SharedMySQLContainerIT extends AbstractMySQLContainerTest {

    @Test
    void testContainerIsRunning() {
        assertTrue(SharedMySQLContainer.isStarted(), "Container should be started by Initializer");
    }

    @Test
    void testContainerProperties() {
        assertNotNull(SharedMySQLContainer.getJdbcUrl(), "JDBC URL should not be null");
        assertNotNull(SharedMySQLContainer.getUsername(), "Username should not be null");
        assertNotNull(SharedMySQLContainer.getPassword(), "Password should not be null");
        assertNotNull(SharedMySQLContainer.getDatabaseName(), "Database name should not be null");
        assertNotNull(SharedMySQLContainer.getDriverClassName(), "Driver class name should not be null");
        assertNotNull(SharedMySQLContainer.getHost(), "Host should not be null");
        assertNotNull(SharedMySQLContainer.getMappedPort(), "Mapped port should not be null");

        LogUtil.info(SharedMySQLContainerIT.class, "JDBC URL: {}", SharedMySQLContainer.getJdbcUrl());
        LogUtil.info(SharedMySQLContainerIT.class, "Username: {}", SharedMySQLContainer.getUsername());
        LogUtil.info(SharedMySQLContainerIT.class, "Database: {}", SharedMySQLContainer.getDatabaseName());
        LogUtil.info(SharedMySQLContainerIT.class, "Host: {}", SharedMySQLContainer.getHost());
        LogUtil.info(SharedMySQLContainerIT.class, "Port: {}", SharedMySQLContainer.getMappedPort());
    }

    @Test
    void testDatabaseConnection() throws Exception {
        String jdbcUrl = SharedMySQLContainer.getJdbcUrl();
        String username = SharedMySQLContainer.getUsername();
        String password = SharedMySQLContainer.getPassword();

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
        String jdbcUrl = SharedMySQLContainer.getJdbcUrl();
        String username = SharedMySQLContainer.getUsername();
        String password = SharedMySQLContainer.getPassword();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            try (Statement statement = connection.createStatement()) {
                // Create a test table
                statement.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(100))");

                // Insert a record
                statement.execute(
                        "INSERT INTO test_table (id, name) VALUES (1, 'test') ON DUPLICATE KEY UPDATE name='test'");

                // Query the record
                ResultSet resultSet = statement.executeQuery("SELECT * FROM test_table WHERE id = 1");
                assertTrue(resultSet.next(), "Should find the inserted record");
                assertEquals(1, resultSet.getInt("id"), "ID should be 1");
                assertEquals("test", resultSet.getString("name"), "Name should be 'test'");

                // Clean up
                statement.execute("DROP TABLE test_table");
            }
        }
    }

    @Test
    void testSameContainerAcrossTests() {
        @SuppressWarnings("rawtypes")
        MySQLContainer container1 = SharedMySQLContainer.getInstance();
        @SuppressWarnings("rawtypes")
        MySQLContainer container2 = SharedMySQLContainer.getInstance();

        assertSame(container1, container2, "Should return the same container instance");
    }
}
