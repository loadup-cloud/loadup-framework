package io.github.loadup.components.database.flyway;

/*-
 * #%L
 * Loadup Components Flyway
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
    public void handle(Event event, Context context) {
        try (Connection connection = context.getConnection();
                Statement statement = connection.createStatement()) {
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
