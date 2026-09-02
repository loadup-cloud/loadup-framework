/*-
 * #%L
 * Loadup Components Flyway
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

package io.github.loadup.components.database.flyway;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.Statement;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;

/**
 * Flyway callback that executes an initialization SQL statement on each
 * connection immediately after it is obtained.
 *
 * <p>Replaces the deprecated {@code FluentConfiguration.initSql()} API
 * with the callback-based approach recommended in Flyway 12.x+.
 *
 * @since 1.0.0
 */
public class StatementInitCallback implements Callback {

    private final String sql;

    public StatementInitCallback(String sql) {
        this.sql = sql;
    }

    @Override
    public boolean supports(Event event, Context context) {
        return event == Event.AFTER_CONNECT;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    @SuppressFBWarnings(
            value = "SQL_INJECTION_JDBC",
            justification =
                    "SQL comes from operator-configured FlywayProperties.initSqls at startup, not runtime input;"
                            + " this is the Flyway initSql replacement API.")
    public void handle(Event event, Context context) {
        Connection connection = context.getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute init SQL: " + sql, e);
        }
    }

    @Override
    public String getCallbackName() {
        return "InitSQL-" + Integer.toHexString(sql.hashCode());
    }
}
