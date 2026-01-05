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
package com.github.loadup.components.testcontainers.database;

import org.springframework.test.context.ContextConfiguration;

/**
 * Abstract base test class that automatically configures PostgreSQL TestContainer.
 *
 * <p>Test classes can extend this class to automatically use the shared PostgreSQL container
 * without needing to manually configure the context initializer.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@ContextConfiguration(initializers = PostgreSQLContainerInitializer.class)
public abstract class AbstractPostgreSQLContainerTest {

  protected String getJdbcUrl() {
    return SharedPostgreSQLContainer.getJdbcUrl();
  }

  protected String getUsername() {
    return SharedPostgreSQLContainer.getUsername();
  }

  protected String getPassword() {
    return SharedPostgreSQLContainer.getPassword();
  }

  protected String getDatabaseName() {
    return SharedPostgreSQLContainer.getDatabaseName();
  }

  protected String getHost() {
    return SharedPostgreSQLContainer.getHost();
  }

  protected Integer getMappedPort() {
    return SharedPostgreSQLContainer.getMappedPort();
  }
}
