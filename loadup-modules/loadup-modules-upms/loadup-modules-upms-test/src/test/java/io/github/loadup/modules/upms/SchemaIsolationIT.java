/*
 * #%L
 * Loadup UPMS Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
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
 * #L%
 */
package io.github.loadup.modules.upms;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Guards the schema-per-tenant contract: business tables must not carry tenant columns.
 */
@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(ContainerType.POSTGRESQL)
class SchemaIsolationIT {
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void businessTablesDoNotDefineTenantColumns() {
        List<String> columns = jdbc.queryForList("""
            SELECT table_schema || '.' || table_name
            FROM information_schema.columns
            WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
              AND column_name = 'tenant_id'
            """, String.class);
        assertThat(columns).isEmpty();
    }

    @Test
    void migrationsTargetTheDefaultSchema() {
        assertThat(jdbc.queryForObject("SELECT current_schema()", String.class)).isEqualTo("public");
    }
}
