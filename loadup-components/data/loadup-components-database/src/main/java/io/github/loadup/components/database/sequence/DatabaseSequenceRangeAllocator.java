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
package io.github.loadup.components.database.sequence;

import io.github.loadup.commons.error.CommonException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Globally shared sequence rows, isolated from caller tenant and business rollback.
 */
public final class DatabaseSequenceRangeAllocator implements SequenceRangeAllocator {
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final TransactionTemplate transaction;

    public DatabaseSequenceRangeAllocator(JdbcTemplate jdbc, PlatformTransactionManager manager, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
        transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transaction.setTimeout(3);
    }

    public SequenceRange reserve(SequenceScope scope, int step) {
        if (scope == null || step < 1) {
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        }
        for (int attempt = 0; attempt < 8; attempt++) {
            try {
                return transaction.execute(status -> allocate(scope, step));
            } catch (TransientDataAccessException conflict) {
                if (attempt == 7) {
                    throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, conflict);
                }
                LockSupport.parkNanos(
                        ThreadLocalRandom.current().nextLong(1_000_000L, Math.min(50_000_000L, 2_000_000L << attempt)));
                if (Thread.currentThread().isInterrupted()) {
                    throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, conflict);
                }
            } catch (DataAccessException | TransactionException failure) {
                throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE, failure);
            }
        }
        throw new CommonException(DatabaseResultCodeEnum.UNAVAILABLE);
    }

    private SequenceRange allocate(SequenceScope scope, int step) {
        LocalDateTime now = LocalDateTime.now(clock);
        jdbc.update("""
            INSERT INTO loadup_sequence_allocation
                (id, created_at, updated_at, deleted, scope_key, max_allocated, max_value)
            VALUES (?, ?, ?, FALSE, ?, 0, ?)
            ON CONFLICT (scope_key) DO NOTHING
            """, UUID.randomUUID().toString(), now, now, scope.key(), scope.maximum());
        Allocation row = jdbc.queryForObject(
                """
                SELECT max_allocated, max_value, deleted FROM loadup_sequence_allocation
                WHERE scope_key = ? FOR UPDATE
                """,
                (rs, index) ->
                        new Allocation(rs.getLong("max_allocated"), rs.getLong("max_value"), rs.getBoolean("deleted")),
                scope.key());
        if (row == null || row.deleted() || row.allocated() < 0 || row.allocated() > row.maximum()) {
            throw new CommonException(DatabaseResultCodeEnum.CORRUPT_STATE);
        }
        if (row.maximum() != scope.maximum()) {
            throw new CommonException(DatabaseResultCodeEnum.CONFIG_CONFLICT);
        }
        long first = row.allocated() + 1;
        if (first > scope.maximum()) {
            throw new CommonException(DatabaseResultCodeEnum.EXHAUSTED);
        }
        long last = row.allocated() + Math.min((long) step, scope.maximum() - row.allocated());
        if (jdbc.update("""
            UPDATE loadup_sequence_allocation SET max_allocated = ?, updated_at = ?
            WHERE scope_key = ? AND max_allocated = ? AND deleted = FALSE
            """, last, LocalDateTime.now(clock), scope.key(), row.allocated()) != 1) {
            throw new CommonException(DatabaseResultCodeEnum.CORRUPT_STATE);
        }
        return new SequenceRange(first, last);
    }

    private record Allocation(long allocated, long maximum, boolean deleted) {}
}
