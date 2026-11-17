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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class FixedSchemaConfigurationTest {

    @Test
    void acceptsBoundedLowercaseIdentifiers() {
        assertThat(FixedSchemaNames.require("customer_a_payment")).isEqualTo("customer_a_payment");
        assertThat(FixedSchemaNames.require("_internal")).isEqualTo("_internal");
        assertThat(FixedSchemaNames.require("a")).isEqualTo("a");
    }

    @Test
    void rejectsMissingMalformedReservedAndSharedSchemas() {
        for (String invalid : new String[] {
            null,
            "",
            " Customer_a",
            "customer_a ",
            "Customer_a",
            "1customer",
            "customer-a",
            "x".repeat(64),
            "public",
            "pg_catalog"
        }) {
            assertThatThrownBy(() -> FixedSchemaNames.require(invalid))
                    .as("schema %s", invalid)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void verifierAcceptsAConnectionResolvingToTheConfiguredSchema() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet result = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT current_schema(), current_setting('search_path')")).thenReturn(result);
        when(result.next()).thenReturn(true);
        when(result.getString(1)).thenReturn("customer_a_payment");
        when(result.getString(2)).thenReturn("customer_a_payment");

        assertThatCode(() -> new FixedSchemaVerifier(dataSource, "customer_a_payment").verify())
                .doesNotThrowAnyException();
    }

    @Test
    void verifierRejectsAConnectionBoundToAnotherSchema() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet result = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT current_schema(), current_setting('search_path')")).thenReturn(result);
        when(result.next()).thenReturn(true);
        when(result.getString(1)).thenReturn("public");

        assertThatThrownBy(() -> new FixedSchemaVerifier(dataSource, "customer_a_payment").verify())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("customer_a_payment");
    }

    @Test
    void verifierRejectsAMissingSchema() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet result = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT current_schema(), current_setting('search_path')")).thenReturn(result);
        when(result.next()).thenReturn(true);
        when(result.getString(1)).thenReturn(null);

        assertThatThrownBy(() -> new FixedSchemaVerifier(dataSource, "customer_a_payment").verify())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void environmentAppliesTheConfiguredSchemaToThePool() {
        var environment = new MockEnvironment().withProperty("loadup.database.schema", "customer_a_payment");

        new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.hikari.schema")).isEqualTo("customer_a_payment");
    }

    @Test
    void environmentAcceptsAnIdenticalPoolSchema() {
        var environment = new MockEnvironment()
                .withProperty("loadup.database.schema", "customer_a_payment")
                .withProperty("spring.datasource.hikari.schema", "customer_a_payment");

        assertThatCode(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                .doesNotThrowAnyException();
    }

    @Test
    void environmentRejectsAConflictingPoolSchema() {
        var environment = new MockEnvironment()
                .withProperty("loadup.database.schema", "customer_a_payment")
                .withProperty("spring.datasource.hikari.schema", "customer_b_payment");

        assertThatThrownBy(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("conflicts");
    }

    @Test
    void environmentRejectsAConflictingJdbcCurrentSchema() {
        var environment = new MockEnvironment()
                .withProperty("loadup.database.schema", "customer_a_payment")
                .withProperty(
                        "spring.datasource.url",
                        "jdbc:postgresql://localhost:5432/payments?currentSchema=customer_b_payment");

        assertThatThrownBy(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("currentSchema");
    }

    @Test
    void environmentAcceptsAMatchingJdbcCurrentSchema() {
        var environment = new MockEnvironment()
                .withProperty("loadup.database.schema", "customer_a_payment")
                .withProperty(
                        "spring.datasource.url",
                        "jdbc:postgresql://localhost:5432/payments?currentSchema=customer_a_payment");

        assertThatCode(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                .doesNotThrowAnyException();
    }

    @Test
    void environmentRejectsAConflictingSearchPathOption() {
        var environment = new MockEnvironment()
                .withProperty("loadup.database.schema", "customer_a_payment")
                .withProperty(
                        "spring.datasource.url",
                        "jdbc:postgresql://localhost:5432/payments?options=-c%20search_path%3Dcustomer_b_payment");

        assertThatThrownBy(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("options");
    }

    @Test
    void environmentLeavesAMissingSchemaToTheBindingValidation() {
        var environment = new MockEnvironment();

        assertThatCode(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                .doesNotThrowAnyException();
        assertThat(environment.getProperty("spring.datasource.hikari.schema")).isNull();
    }

    @Test
    void rejectsAdditionalOrDisguisedConnectionSearchPaths() {
        for (String initSql : new String[] {
            "SET search_path TO customer_a_payment,public",
            "SET search_path TO customer_a_payment_backup",
            "SELECT set_config('search' || '_path', 'public', false)",
            "SET search_path TO public /* customer_a_payment */"
        }) {
            var environment = new MockEnvironment()
                    .withProperty("loadup.database.schema", "customer_a_payment")
                    .withProperty("spring.datasource.hikari.connection-init-sql", initSql);
            assertThatThrownBy(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("connection-init-sql");
        }
        for (String property : new String[] {"spring.datasource.url", "spring.datasource.hikari.jdbc-url"}) {
            for (String query : new String[] {
                "options=-c%20search_path%3Dcustomer_a_payment,public",
                "options=-c%20search_path%3Dcustomer_a_payment_backup",
                "currentSchema=customer_a_payment,public",
                "currentSchema=customer_a_payment&currentSchema=public",
                "current%53chema=public"
            }) {
                var environment = new MockEnvironment()
                        .withProperty("loadup.database.schema", "customer_a_payment")
                        .withProperty(property, "jdbc:postgresql://localhost/payments?" + query);
                assertThatThrownBy(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                        .isInstanceOf(IllegalStateException.class);
            }
        }
    }

    @Test
    void rejectsSchemaOverridesInDriverProperties() {
        for (String property : new String[] {"options", "currentSchema"}) {
            var environment = new MockEnvironment()
                    .withProperty("loadup.database.schema", "customer_a_payment")
                    .withProperty("spring.datasource.hikari.data-source-properties." + property,
                            java.util.Objects.equals(property, "options") ? "-c search_path=customer_a_payment,public" : "public");
            assertThatThrownBy(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void relaxedPoolPropertyNamesCannotBypassValidation() {
        for (String property : new String[] {
            "spring.datasource.hikari.connection-init-sql", "spring.datasource.hikari.connectionInitSql"
        }) {
            var environment = new MockEnvironment()
                    .withProperty("loadup.database.schema", "customer_a_payment")
                    .withProperty(property, "SET search_path TO customer_a_payment, public");
            assertThatThrownBy(() -> new FixedSchemaEnvironmentPostProcessor().postProcessEnvironment(environment, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("connection-init-sql");
        }
    }

    @Test
    void verifierRejectsFallbackSchemasEvenWhenTheFirstSchemaMatches() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet result = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT current_schema(), current_setting('search_path')")).thenReturn(result);
        when(result.next()).thenReturn(true);
        when(result.getString(1)).thenReturn("customer_a_payment");
        for (String path : new String[] {"customer_a_payment, public", "customer_a_payment, customer_b_payment", "customer_a_payment, missing_schema"}) {
            when(result.getString(2)).thenReturn(path);
            assertThatThrownBy(() -> new FixedSchemaVerifier(dataSource, "customer_a_payment").verify())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("search_path");
        }
        when(result.getString(2)).thenReturn("\"customer_a_payment\"");
        assertThatCode(() -> new FixedSchemaVerifier(dataSource, "customer_a_payment").verify()).doesNotThrowAnyException();
    }

}
