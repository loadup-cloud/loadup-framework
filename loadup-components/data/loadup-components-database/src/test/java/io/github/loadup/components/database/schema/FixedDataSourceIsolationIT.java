/*
 * Copyright (C) 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.database.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.loadup.components.database.autoconfigure.DatabaseCoreAutoConfiguration;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = FixedDataSourceIsolationIT.Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
class FixedDataSourceIsolationIT {
    private final DataSource administrator;
    private HikariDataSource first;
    private HikariDataSource second;

    @Autowired
    FixedDataSourceIsolationIT(DataSource administrator) {
        this.administrator = administrator;
    }

    @BeforeEach
    void provisionIndependentRuntimeRoles() {
        script("cleanup.sql");
        script("setup.sql");
        first = pool("schema_isolation_a");
        second = pool("schema_isolation_b");
    }

    @AfterEach
    void cleanup() {
        if (first != null) {
            first.close();
        }
        if (second != null) {
            second.close();
        }
        script("cleanup.sql");
    }

    @Test
    void identicalBusinessKeysResolveOnlyWithinEachFixedSchema() {
        new FixedSchemaVerifier(first, "schema_isolation_a").verify();
        new FixedSchemaVerifier(second, "schema_isolation_b").verify();
        assertThat(marker(first)).isEqualTo("deployment-a");
        assertThat(marker(second)).isEqualTo("deployment-b");
        new JdbcTemplate(first).update("UPDATE business_probe SET marker = ? WHERE id = ?", "updated-a", "same-id");
        assertThat(marker(first)).isEqualTo("updated-a");
        assertThat(marker(second)).isEqualTo("deployment-b");
    }

    @Test
    void databaseRejectsCrossSchemaReadsWritesAndRuntimeDdlInBothDirections() throws SQLException {
        denied(first, "SELECT * FROM schema_isolation_b.business_probe");
        denied(first, "UPDATE schema_isolation_b.business_probe SET marker = 'wrong'");
        denied(second, "SELECT * FROM schema_isolation_a.business_probe");
        denied(second, "INSERT INTO schema_isolation_a.business_probe (id, marker) VALUES ('other', 'wrong')");
        denied(first, "CREATE TABLE schema_isolation_a.forbidden (id INTEGER)");
        denied(second, "CREATE TABLE schema_isolation_b.forbidden (id INTEGER)");
        assertThat(marker(first)).isEqualTo("deployment-a");
        assertThat(marker(second)).isEqualTo("deployment-b");
    }

    @Test
    void poolReuseAndReplacementKeepTheConfiguredSchema() throws SQLException {
        int initialPid = new JdbcTemplate(first).queryForObject("SELECT pg_backend_pid()", Integer.class);
        try (Connection connection = first.getConnection()) {
            connection.setSchema("schema_isolation_b");
        }
        assertThat(new JdbcTemplate(first).queryForObject("SELECT pg_backend_pid()", Integer.class))
                .isEqualTo(initialPid);
        new FixedSchemaVerifier(first, "schema_isolation_a").verify();
        assertThat(marker(first)).isEqualTo("deployment-a");
        first.getHikariPoolMXBean().softEvictConnections();
        assertThat(new JdbcTemplate(first).queryForObject("SELECT pg_backend_pid()", Integer.class))
                .isNotEqualTo(initialPid);
        new FixedSchemaVerifier(first, "schema_isolation_a").verify();
        assertThat(marker(first)).isEqualTo("deployment-a");
        assertThat(marker(second)).isEqualTo("deployment-b");
    }

    @Test
    void verifierRejectsAnAdditionalSearchPathFromACustomDataSource() throws SQLException {
        try (Connection connection = first.getConnection();
                var statement = connection.createStatement()) {
            statement.execute("SET search_path TO schema_isolation_a, public");
        }
        assertThatThrownBy(() -> new FixedSchemaVerifier(first, "schema_isolation_a").verify())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("search_path");
    }

    @Test
    void bootCreatedDataSourceRegistersAndRunsTheVerifier() {
        runner("schema_isolation_a", "schema_isolation_a").run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(FixedSchemaVerifier.class);
            assertThat(marker(context.getBean(DataSource.class))).isEqualTo("deployment-a");
        });
        runner("schema_isolation_b", "schema_isolation_a").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalStateException.class);
        });
    }

    private ApplicationContextRunner runner(String expectedSchema, String poolSchema) {
        return new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(DataSourceAutoConfiguration.class, DatabaseCoreAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.database.schema=" + expectedSchema,
                        "spring.datasource.url=" + SharedPostgreSQLContainer.getJdbcUrl(),
                        "spring.datasource.username=schema_isolation_a",
                        "spring.datasource.password=isolation-test-only",
                        "spring.datasource.hikari.schema=" + poolSchema);
    }

    private static HikariDataSource pool(String schema) {
        var environment = new MockEnvironment().withProperty("loadup.database.schema", schema);
        new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null);
        var config = new HikariConfig();
        config.setJdbcUrl(SharedPostgreSQLContainer.getJdbcUrl());
        config.setUsername(schema);
        config.setPassword("isolation-test-only");
        config.setSchema(environment.getRequiredProperty("spring.datasource.hikari.schema"));
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(0);
        return new HikariDataSource(config);
    }

    private static String marker(DataSource source) {
        return new JdbcTemplate(source)
                .queryForObject("SELECT marker FROM business_probe WHERE id = ?", String.class, "same-id");
    }

    private static void denied(DataSource source, String sql) throws SQLException {
        try (Connection connection = source.getConnection();
                var statement = connection.createStatement()) {
            assertThatThrownBy(() -> statement.execute(sql))
                    .isInstanceOfSatisfying(
                            SQLException.class,
                            error -> assertThat(error.getSQLState()).isEqualTo("42501"));
        }
    }

    private void script(String file) {
        new ResourceDatabasePopulator(new ClassPathResource("db/schema-isolation/" + file)).execute(administrator);
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(DataSourceAutoConfiguration.class)
    static class Application {}
}
