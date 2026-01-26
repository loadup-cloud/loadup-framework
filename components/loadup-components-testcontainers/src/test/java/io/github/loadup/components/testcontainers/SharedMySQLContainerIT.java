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
package io.github.loadup.components.testcontainers;

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.components.testcontainers.database.AbstractMySQLContainerTest;
import io.github.loadup.components.testcontainers.database.SharedMySQLContainer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Integration test class for SharedMySQLContainer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(
    properties = {"loadup.testcontainers.enabled=true", "loadup.testcontainers.mysql.enabled=true"})
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
    assertNotNull(
        SharedMySQLContainer.getDriverClassName(), "Driver class name should not be null");
    assertNotNull(SharedMySQLContainer.getHost(), "Host should not be null");
    assertNotNull(SharedMySQLContainer.getMappedPort(), "Mapped port should not be null");

    log.info("JDBC URL: {}", SharedMySQLContainer.getJdbcUrl());
    log.info("Username: {}", SharedMySQLContainer.getUsername());
    log.info("Database: {}", SharedMySQLContainer.getDatabaseName());
    log.info("Host: {}", SharedMySQLContainer.getHost());
    log.info("Port: {}", SharedMySQLContainer.getMappedPort());
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
        statement.execute(
            "CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(100))");

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
