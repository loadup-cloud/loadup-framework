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

import io.github.loadup.components.testcontainers.BaseContainerInitializer;
import io.github.loadup.components.testcontainers.config.TestContainersProperties;
import io.github.loadup.components.testcontainers.config.TestContainersProperties.ContainerConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Boot test initializer for MySQL TestContainer.
 *
 * <p>This initializer configures Spring Boot test context to use the shared MySQL container. It
 * automatically sets the necessary datasource properties.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#64;SpringBootTest
 * &#64;ContextConfiguration(initializers = MySQLContainerInitializer.class)
 * class MyIntegrationTest {
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
@Slf4j
public class MySQLContainerInitializer extends BaseContainerInitializer {

  @Override
  protected String getContainerName() {
    return "MySQL";
  }

  @Override
  protected ContainerConfig getContainerConfig(TestContainersProperties p) {
    return p.getMysql();
  }

  @Override
  protected void startAndApplyProperties(ContainerConfig config, ConfigurableEnvironment env) {
    SharedMySQLContainer.startContainer(config);
    applyProperties(
        env,
        Map.of(
            "spring.datasource.url",
            SharedMySQLContainer.getJdbcUrl(),
            "spring.datasource.username",
            SharedMySQLContainer.getUsername(),
            "spring.datasource.password",
            SharedMySQLContainer.getPassword(),
            "spring.datasource.driver-class-name",
            SharedMySQLContainer.getDriverClassName()));
  }
}
