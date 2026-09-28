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
