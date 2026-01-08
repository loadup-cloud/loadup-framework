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
package com.github.loadup.components.testcontainers;

import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.testcontainers.database.AbstractPostgreSQLContainerTest;
import com.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
    assertNotNull(
        SharedPostgreSQLContainer.getDriverClassName(), "Driver class name should not be null");
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
        statement.execute(
            "CREATE TABLE IF NOT EXISTS test_table_pg (id INT PRIMARY KEY, name VARCHAR(100))");

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
