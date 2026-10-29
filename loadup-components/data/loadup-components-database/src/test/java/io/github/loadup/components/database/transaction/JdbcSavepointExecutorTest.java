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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class JdbcSavepointExecutorTest {
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final Savepoint savepoint = mock(Savepoint.class);

    @AfterEach
    void clearTransaction() {
        if (TransactionSynchronizationManager.hasResource(dataSource))
            TransactionSynchronizationManager.unbindResource(dataSource);
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    private void bindTransaction() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);
        when(connection.setSavepoint()).thenReturn(savepoint);
        TransactionSynchronizationManager.bindResource(dataSource, new ConnectionHolder(connection));
        TransactionSynchronizationManager.setActualTransactionActive(true);
    }

    @Test
    void duplicateIsRolledBackToSavepointBeforeItIsReported() throws SQLException {
        bindTransaction();
        var duplicate = new DuplicateKeyException("duplicate");
        assertSame(
                duplicate,
                assertThrows(
                        DuplicateKeyException.class,
                        () -> JdbcSavepointExecutor.execute(dataSource, () -> {
                            throw duplicate;
                        })));
        var order = inOrder(connection);
        order.verify(connection).setSavepoint();
        order.verify(connection).rollback(savepoint);
        order.verify(connection).releaseSavepoint(savepoint);
    }

    @Test
    void successfulStatementReleasesSavepointWithoutRollingBack() throws SQLException {
        bindTransaction();
        assertEquals(1, JdbcSavepointExecutor.execute(dataSource, () -> 1));
        verify(connection).releaseSavepoint(savepoint);
        verify(connection, never()).rollback(savepoint);
    }

    @Test
    void activeTransactionCannotUseAnAutocommitConnection() throws SQLException {
        bindTransaction();
        when(connection.getAutoCommit()).thenReturn(true);
        var invoked = new java.util.concurrent.atomic.AtomicBoolean();
        assertThrows(
                DataAccessResourceFailureException.class,
                () -> JdbcSavepointExecutor.execute(dataSource, () -> {
                    invoked.set(true);
                    return null;
                }));
        assertFalse(invoked.get());
        verify(connection, never()).setSavepoint();
    }

    @Test
    void rollbackFailureCannotBeMisreportedAsAnExpectedDuplicate() throws SQLException {
        bindTransaction();
        doThrow(new SQLException("savepoint rollback failed")).when(connection).rollback(savepoint);
        assertThrows(
                DataAccessResourceFailureException.class,
                () -> JdbcSavepointExecutor.execute(dataSource, () -> {
                    throw new DuplicateKeyException("duplicate");
                }));
    }

    @Test
    void releaseFailureDoesNotHideTheOriginalFailureOrReportADuplicate() throws SQLException {
        bindTransaction();
        var duplicate = new DuplicateKeyException("duplicate");
        var release = new SQLException("savepoint release failed");
        doThrow(release).when(connection).releaseSavepoint(savepoint);
        var error = assertThrows(
                DataAccessResourceFailureException.class,
                () -> JdbcSavepointExecutor.execute(dataSource, () -> {
                    throw duplicate;
                }));
        assertSame(release, error.getCause());
        assertArrayEquals(new Throwable[] {duplicate}, release.getSuppressed());
    }

    @Test
    void withoutOuterTransactionTheStatementUsesItsNormalConnection() throws SQLException {
        assertEquals(1, JdbcSavepointExecutor.execute(dataSource, () -> 1));
        verifyNoInteractions(dataSource, connection);
    }
}
