/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask;

import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.delayedtask.dataobject.DelayedTaskDO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * PostgreSQL task storage; claims and acknowledgements use short fenced transactions.
 */
public class DelayedTaskStore {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;

    public DelayedTaskStore(JdbcTemplate jdbc, IdGenerator ids) {
        this.jdbc = jdbc;
        this.ids = ids;
    }

    private static DelayedTaskDO map(ResultSet rs, int index) throws SQLException {
        var row = new DelayedTaskDO();
        row.setId(rs.getString("id"));
        row.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        row.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        row.setTaskType(rs.getString("task_type"));
        row.setBizId(rs.getString("biz_id"));
        row.setExecuteAt(rs.getObject("execute_at", LocalDateTime.class));
        row.setNextExecuteAt(rs.getObject("next_execute_at", LocalDateTime.class));
        row.setStatus(DelayedTaskStatus.valueOf(rs.getString("status")));
        row.setAttempts(rs.getInt("attempts"));
        row.setMaxAttempts(rs.getInt("max_attempts"));
        row.setLeaseUntil(rs.getObject("lease_until", LocalDateTime.class));
        row.setClaimToken(rs.getString("claim_token"));
        row.setLastError(rs.getString("last_error"));
        return row;
    }

    DelayedTaskDO insert(String type, String biz, LocalDateTime deadline, int maxAttempts, LocalDateTime now) {
        String id = ids.generate();
        int inserted = jdbc.update("""
            INSERT INTO delayed_task (id, created_at, updated_at, deleted, task_type, biz_id,
                execute_at, next_execute_at, status, attempts, max_attempts)
            VALUES (?, ?, ?, FALSE, ?, ?, ?, ?, 'PENDING', 0, ?)
            ON CONFLICT (task_type, biz_id) DO NOTHING
            """, id, now, now, type, biz, deadline, deadline, maxAttempts);
        if (inserted != 0 && inserted != 1) {
            throw new IllegalStateException("Unexpected task insert count");
        }
        return find(type, biz, inserted == 0)
                .orElseThrow(() -> new IllegalStateException("Task key exists but its active record is unavailable"));
    }

    Optional<DelayedTaskDO> find(String type, String biz, boolean lock) {
        String sql = """
            SELECT * FROM delayed_task
            WHERE task_type = ? AND biz_id = ? AND deleted = FALSE
            """ + (lock ? " FOR UPDATE" : "");
        return jdbc.query(sql, DelayedTaskStore::map, type, biz).stream().findFirst();
    }

    boolean cancel(String type, String biz, LocalDateTime now) {
        return jdbc.update("""
            UPDATE delayed_task SET status = 'CANCELLED', claim_token = NULL, lease_until = NULL, updated_at = ?
            WHERE task_type = ? AND biz_id = ? AND deleted = FALSE
              AND status IN ('PENDING', 'RUNNING', 'FAILED')
            """, now, type, biz) == 1;
    }

    boolean reschedule(String type, String biz, LocalDateTime deadline, boolean retry, LocalDateTime now) {
        if (retry) {
            return jdbc.update("""
                UPDATE delayed_task SET status = 'PENDING', next_execute_at = ?, attempts = 0,
                    last_error = NULL, claim_token = NULL, lease_until = NULL, updated_at = ?
                WHERE task_type = ? AND biz_id = ? AND deleted = FALSE AND status = 'FAILED'
                """, deadline, now, type, biz) == 1;
        }
        return jdbc.update("""
            UPDATE delayed_task SET status = 'PENDING', execute_at = ?, next_execute_at = ?, attempts = 0,
                last_error = NULL, claim_token = NULL, lease_until = NULL, updated_at = ?
            WHERE task_type = ? AND biz_id = ? AND deleted = FALSE
              AND status IN ('PENDING', 'FAILED', 'CANCELLED')
            """, deadline, deadline, now, type, biz) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recoverExhausted(LocalDateTime now) {
        jdbc.update("""
            UPDATE delayed_task SET status = 'FAILED', claim_token = NULL, lease_until = NULL,
                last_error = 'Lease expired after final attempt', updated_at = ?
            WHERE deleted = FALSE AND status = 'RUNNING' AND lease_until <= ? AND attempts >= max_attempts
            """, now, now);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Optional<DelayedTaskDO> claim(LocalDateTime now, DelayedTaskProperties props) {
        var candidates = jdbc.query("""
            SELECT * FROM delayed_task WHERE deleted = FALSE AND next_execute_at <= ?
              AND (status = 'PENDING' OR (status = 'RUNNING' AND lease_until <= ?))
              AND attempts < max_attempts
            ORDER BY next_execute_at, id LIMIT 1 FOR UPDATE SKIP LOCKED
            """, DelayedTaskStore::map, now, now);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        var row = candidates.getFirst();
        String token = UUID.randomUUID().toString();
        LocalDateTime leaseUntil = now.plus(props.getLease());
        int attempts = row.getAttempts() + 1;
        int changed = jdbc.update("""
            UPDATE delayed_task SET status = 'RUNNING', attempts = ?, claim_token = ?, lease_until = ?, updated_at = ?
            WHERE id = ? AND deleted = FALSE AND attempts = ?
            """, attempts, token, leaseUntil, now, row.getId(), row.getAttempts());
        if (changed != 1) {
            throw new IllegalStateException("Locked delayed task changed during claim");
        }
        row.setStatus(DelayedTaskStatus.RUNNING);
        row.setAttempts(attempts);
        row.setClaimToken(token);
        row.setLeaseUntil(leaseUntil);
        return Optional.of(row);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean finish(DelayedTaskDO claimed, LocalDateTime now, boolean success, DelayedTaskProperties props) {
        String state = success ? "SUCCEEDED" : claimed.getAttempts() >= claimed.getMaxAttempts() ? "FAILED" : "PENDING";
        LocalDateTime next = success ? claimed.getNextExecuteAt() : now.plus(props.getRetryDelay());
        return jdbc.update(
                        """
                UPDATE delayed_task SET status = ?, claim_token = NULL, lease_until = NULL, last_error = ?,
                    next_execute_at = ?, updated_at = ?
                WHERE id = ? AND deleted = FALSE AND status = 'RUNNING'
                  AND claim_token = ? AND lease_until > ?
                """,
                        state,
                        success ? null : "Handler failed",
                        next,
                        now,
                        claimed.getId(),
                        claimed.getClaimToken(),
                        now)
                == 1;
    }
}
