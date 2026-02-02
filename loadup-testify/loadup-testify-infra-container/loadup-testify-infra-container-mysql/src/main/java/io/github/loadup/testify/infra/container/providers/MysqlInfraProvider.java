package io.github.loadup.testify.infra.container.providers;

/*-
 * #%L
 * Testify Infra Container Mysql
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

import io.github.loadup.testify.infra.container.InfraProvider;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.mysql.MySQLContainer;

@Slf4j
public class MysqlInfraProvider implements InfraProvider {
  private MySQLContainer mysqlContainer;

  public static final String DEFAULT_MYSQL_VERSION = "mysql:8.0";

  @Override
  public String getType() {
    return "mysql";
  }

  @Override
  @SuppressWarnings("resource")
  public void start(Map<String, Object> config, boolean reuse) {
    String image = (String) config.getOrDefault("image", DEFAULT_MYSQL_VERSION);
    String initScript = (String) config.get("init_script");

    mysqlContainer =
        new MySQLContainer(image)
            .withDatabaseName((String) config.getOrDefault("database", "testify_db"))
            .withUsername("testify")
            .withPassword("testify")
            .withCommand("--max-allowed-packet=268435456")
            // 开启连接复用
            .withReuse(reuse);

    if (null != initScript && !initScript.isEmpty()) {
      mysqlContainer.withInitScript(initScript);
    }

    log.info(">>> [TESTIFY-INFRA] Starting MySQL container: {}", image);
    mysqlContainer.start();
  }

  @Override
  public Map<String, String> getExposedProperties() {
    Map<String, String> props = new HashMap<>();
    if (mysqlContainer != null && mysqlContainer.isRunning()) {
      props.put("spring.datasource.url", mysqlContainer.getJdbcUrl());
      props.put("spring.datasource.username", mysqlContainer.getUsername());
      props.put("spring.datasource.password", mysqlContainer.getPassword());
      // 确保驱动一致性
      props.put("spring.datasource.driver-class-name", mysqlContainer.getDriverClassName());
    }
    return props;
  }

  @Override
  public void stop() {
    if (mysqlContainer != null) {
      mysqlContainer.stop();
    }
  }
}
