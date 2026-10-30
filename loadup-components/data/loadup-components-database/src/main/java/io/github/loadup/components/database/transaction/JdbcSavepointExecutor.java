/*
 * Copyright 2026 LoadUp Cloud
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
package io.github.loadup.components.database.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Objects;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Isolates a single statement failure within the current JDBC transaction.
 */
public final class JdbcSavepointExecutor {
    private JdbcSavepointExecutor() {}

    public static <T> T execute(DataSource dataSource, Supplier<T> statement) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(statement, "statement");
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return statement.get();
        }
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            if (!DataSourceUtils.isConnectionTransactional(connection, dataSource) || connection.getAutoCommit()) {
                throw new DataAccessResourceFailureException(
                        "The current transaction must use the same JDBC DataSource");
            }
            Savepoint savepoint = connection.setSavepoint();
            Throwable priorFailure = null;
            try {
                return statement.get();
            } catch (RuntimeException | Error failure) {
                try {
                    connection.rollback(savepoint);
                } catch (SQLException rollbackFailure) {
                    rollbackFailure.addSuppressed(failure);
                    var error = new DataAccessResourceFailureException(
                            "Unable to roll back JDBC savepoint", rollbackFailure);
                    priorFailure = error;
                    throw error;
                }
                priorFailure = failure;
                throw failure;
            } finally {
                try {
                    connection.releaseSavepoint(savepoint);
                } catch (SQLException releaseFailure) {
                    if (priorFailure != null) {
                        releaseFailure.addSuppressed(priorFailure);
                    }
                    throw new DataAccessResourceFailureException("Unable to release JDBC savepoint", releaseFailure);
                }
            }
        } catch (SQLException failure) {
            throw new DataAccessResourceFailureException("Unable to manage JDBC savepoint", failure);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
