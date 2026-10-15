/*
 * #%L
 * Loadup Components Database
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.junit.jupiter.api.Test;

class StatementInitCallbackTest {
    @Test
    void runsOnlyAfterConnectAndProvidesStableSqlSpecificIdentity() {
        var callback = new StatementInitCallback("SET time_zone = '+08:00'");
        for (Event event : Event.values()) {
            assertThat(callback.supports(event, null)).isEqualTo(event == Event.AFTER_CONNECT);
            assertThat(callback.canHandleInTransaction(event, null)).isTrue();
        }
        assertThat(callback.getCallbackName())
                .isEqualTo(new StatementInitCallback("SET time_zone = '+08:00'").getCallbackName())
                .isNotEqualTo(new StatementInitCallback("SET time_zone = '+09:00'").getCallbackName());
    }

    @Test
    void executesConfiguredSqlAndClosesOnlyTheStatement() throws Exception {
        Context context = mock(Context.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(context.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        new StatementInitCallback("SET time_zone = '+08:00'").handle(Event.AFTER_CONNECT, context);

        verify(statement).execute("SET time_zone = '+08:00'");
        verify(statement).close();
        org.mockito.Mockito.verify(connection, org.mockito.Mockito.never()).close();
    }

    @Test
    void preservesExecutionAndReleaseFailuresAsSystemErrorCause() throws Exception {
        Context context = mock(Context.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        SQLException failure = new SQLException("synthetic SQL failure");
        SQLException release = new SQLException("synthetic close failure");
        when(context.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.execute("SELECT 1")).thenThrow(failure);
        doThrow(release).when(statement).close();

        assertThatThrownBy(() -> new StatementInitCallback("SELECT 1").handle(Event.AFTER_CONNECT, context))
                .isInstanceOf(CommonException.class)
                .hasCause(failure)
                .satisfies(error -> assertThat(((CommonException) error).getResultCode())
                        .isEqualTo(CommonResultCodeEnum.SYS_ERROR));
        assertThat(failure.getSuppressed()).containsExactly(release);
        verify(statement).close();
    }

    @Test
    void reportsStatementAcquisitionFailureWithoutClosingTheFlywayConnection() throws Exception {
        Context context = mock(Context.class);
        Connection connection = mock(Connection.class);
        SQLException failure = new SQLException("connection unavailable");
        when(context.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenThrow(failure);

        assertThatThrownBy(() -> new StatementInitCallback("SELECT 1").handle(Event.AFTER_CONNECT, context))
                .isInstanceOf(CommonException.class)
                .hasCause(failure);
        org.mockito.Mockito.verify(connection, org.mockito.Mockito.never()).close();
    }
}
