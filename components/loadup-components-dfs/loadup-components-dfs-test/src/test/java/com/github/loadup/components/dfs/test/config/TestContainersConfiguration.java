package com.github.loadup.components.dfs.test.config;

/*-
 * #%L
 * loadup-components-dfs-test
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/** Testcontainers配置 - 为测试提供MySQL容器 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {

  /** MySQL容器配置 使用@ServiceConnection自动配置Spring Boot的DataSource */
  @Bean
  @ServiceConnection
  public MySQLContainer<?> mysqlContainer() {
    return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
        .withDatabaseName("dfs_test")
        .withUsername("test")
        .withPassword("test")
        .withReuse(false) // 测试后自动清理
        .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");
  }
}
