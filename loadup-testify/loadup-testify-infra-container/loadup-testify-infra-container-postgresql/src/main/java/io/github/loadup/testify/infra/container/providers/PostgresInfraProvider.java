package io.github.loadup.testify.infra.container.providers;

/*-
 * #%L
 * Testify Infra Container Postgresql
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
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Slf4j
public class PostgresInfraProvider implements InfraProvider {
    public static final String DEFAULT_POSTGRES_VERSION = "postgres:15-alpine";

    private PostgreSQLContainer container;

    @Override
    public String getType() {
        return "postgresql";
    }

    @Override
    public void start(Map<String, Object> config, boolean reuse) {
        var image = (String) config.getOrDefault("image", DEFAULT_POSTGRES_VERSION);

        container = new PostgreSQLContainer(image)
                .withDatabaseName((String) config.getOrDefault("database", "testify_db"))
                .withUsername("testify")
                .withPassword("testify")
                .withReuse(reuse);

        // 支持初始化脚本
        Optional.ofNullable(config.get("init_script")).map(Object::toString).ifPresent(container::withInitScript);

        container.start();
    }

    @Override
    public Map<String, String> getExposedProperties() {
        if (container == null || !container.isRunning()) return Map.of();

        return Map.of(
                "spring.datasource.url", container.getJdbcUrl(),
                "spring.datasource.username", container.getUsername(),
                "spring.datasource.password", container.getPassword(),
                "spring.datasource.driver-class-name", container.getDriverClassName());
    }

    @Override
    public void stop() {
        if (container != null) container.stop();
    }
}
