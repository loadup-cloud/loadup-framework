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
package io.github.loadup.components.testcontainers.database;

import org.springframework.test.context.ContextConfiguration;

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
@ContextConfiguration(initializers = MySQLContainerInitializer.class)
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
