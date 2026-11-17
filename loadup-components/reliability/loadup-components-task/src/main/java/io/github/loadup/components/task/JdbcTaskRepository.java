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
package io.github.loadup.components.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

public class JdbcTaskRepository implements TaskTemplate {
    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final TaskProperties properties;

    public JdbcTaskRepository(DataSource dataSource, PlatformTransactionManager manager, TaskProperties properties) {
        this.dataSource = dataSource;
        this.jdbc = new JdbcTemplate(dataSource);
        this.properties = properties;
        properties.validate();
        requireManager(dataSource, manager);
        transactions = new TransactionTemplate(manager);
        transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactions.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    static void requireManager(DataSource dataSource, PlatformTransactionManager manager) {
        if (!(manager instanceof DataSourceTransactionManager jdbcManager)
                || jdbcManager.getDataSource() != dataSource) {
            throw new IllegalArgumentException("Task requires a JDBC transaction manager for its DataSource");
        }
    }

    static void requireTransaction(DataSource dataSource) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || !TransactionSynchronizationManager.hasResource(dataSource)) {
            throw new IllegalStateException("Task requires a writable transaction on its DataSource");
        }
        var connection = DataSourceUtils.getConnection(dataSource);
        try {
            if (connection.getAutoCommit()) {
                throw new IllegalStateException("Task DataSource is not participating in the transaction");
            }
        } catch (SQLException failure) {
            throw new IllegalStateException("Cannot inspect task transaction", failure);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static TaskExecution event(ResultSet rs) throws SQLException {
        return new TaskExecution(
                rs.getString("id"),
                rs.getString("task_type"),
                rs.getString("biz_id"),
                rs.getTimestamp("execute_at").toLocalDateTime(),
                rs.getTimestamp("next_execute_at").toLocalDateTime(),
                TaskStatus.valueOf(rs.getString("status")),
                rs.getInt("attempts"),
                rs.getInt("max_attempts"),
                rs.getTimestamp("lease_until") == null
                        ? null
                        : rs.getTimestamp("lease_until").toLocalDateTime(),
                rs.getString("last_error"),
                rs.getInt("payload_version"),
                rs.getString("payload"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getString("trace_id"));
    }

    private static TaskExecution record(ResultSet rs, int ignored) throws SQLException {
        return event(rs);
    }

    @Override
    public String submit(TaskSubmission task) {
        requireTransaction(dataSource);
        if (task == null) throw new IllegalArgumentException("task is required");
        var policy = properties.policy(task.taskType());
        policy.validate();
        String id = UUID.randomUUID().toString();
        String trace = MDC.get("traceId");
        if (trace != null && !trace.matches("[a-fA-F0-9]{16,32}")) trace = null;
        int inserted = jdbc.update(
                """
            INSERT INTO loadup_task(id,created_at,updated_at,task_type,payload_version,biz_id,payload,trace_id,
                status,execute_at,next_execute_at,dedup_key,max_attempts,retry_delay_ms,lease_ms,exponential)
            VALUES (?,clock_timestamp(),clock_timestamp(),?,?,?,?,?,'PENDING',
                COALESCE(?,clock_timestamp()),COALESCE(?,clock_timestamp()),?,?,?,?,?)
            ON CONFLICT (dedup_key) DO NOTHING
            """,
                id,
                task.taskType(),
                task.payloadVersion(),
                task.bizId(),
                task.payload(),
                trace,
                task.executeAt(),
                task.executeAt(),
                task.dedupKey(),
                policy.getMaxAttempts(),
                policy.getRetryDelay().toMillis(),
                policy.getLease().toMillis(),
                policy.isExponential());
        if (inserted == 1) return id;
        var rows = jdbc.query(
                "SELECT * FROM loadup_task WHERE dedup_key = ? AND deleted = FALSE FOR UPDATE",
                JdbcTaskRepository::record,
                task.dedupKey());
        if (rows.isEmpty()) throw new IllegalStateException("Task deduplication record is unavailable");
        var existing = rows.getFirst();
        if (!existing.taskType().equals(task.taskType())
                || !existing.bizId().equals(task.bizId())
                || existing.payloadVersion() != task.payloadVersion()
                || !java.util.Objects.equals(existing.payload(), task.payload())
                || (task.executeAt() != null && !existing.executeAt().equals(task.executeAt()))) {
            throw new io.github.loadup.commons.error.CommonException(TaskResultCodeEnum.CONFLICT);
        }
        return existing.id();
    }

    public Optional<Claim> claim() {
        return claim(null);
    }

    public Optional<Claim> claim(String taskType) {
        return transactions.execute(status -> {
            jdbc.update(
                    "UPDATE loadup_task SET status='FAILED',claim_token=NULL,lease_until=NULL,last_error='LeaseExpired',updated_at=clock_timestamp() WHERE deleted=FALSE AND status='RUNNING' AND lease_until <= clock_timestamp() AND attempts >= max_attempts AND (CAST(? AS VARCHAR) IS NULL OR task_type = ?)",
                    taskType,
                    taskType);
            List<Claim> rows = jdbc.query(
                    "SELECT * FROM loadup_task WHERE deleted = FALSE AND (CAST(? AS VARCHAR) IS NULL OR task_type = ?) AND ("
                            + "(status = 'PENDING' AND next_execute_at <= clock_timestamp()) OR "
                            + "(status = 'RUNNING' AND lease_until <= clock_timestamp())) "
                            + "ORDER BY next_execute_at, id LIMIT 1 FOR UPDATE SKIP LOCKED",
                    (rs, row) -> new Claim(event(rs), UUID.randomUUID().toString(), rs.getInt("attempts") + 1),
                    taskType,
                    taskType);
            if (rows.isEmpty()) {
                return Optional.empty();
            }
            Claim claim = rows.getFirst();
            if (claim.attempt() > claim.event().maxAttempts()) {
                int changed = jdbc.update(
                        "UPDATE loadup_task SET status = 'FAILED', claim_token = NULL, lease_until = NULL, "
                                + "last_error = 'LeaseExpired', updated_at = clock_timestamp() WHERE id = ? AND deleted = FALSE",
                        claim.event().id());
                if (changed != 1) {
                    throw new IllegalStateException("Locked task event changed during recovery");
                }
                return Optional.empty();
            }
            int changed = jdbc.update(
                    "UPDATE loadup_task SET status = 'RUNNING', attempts = ?, claim_token = ?, "
                            + "lease_until = (clock_timestamp() + ? * INTERVAL '1 microsecond'), updated_at = clock_timestamp() "
                            + "WHERE id = ? AND deleted = FALSE",
                    claim.attempt(),
                    claim.token(),
                    leaseMillis(claim.event().id()) * 1000,
                    claim.event().id());
            if (changed != 1) {
                throw new IllegalStateException("Locked task event changed during claim");
            }
            return Optional.of(new Claim(find(claim.event().id()).orElseThrow(), claim.token(), claim.attempt()));
        });
    }

    public boolean complete(Claim claim) {
        return Boolean.TRUE.equals(transactions.execute(status -> jdbc.update(
                        "UPDATE loadup_task SET status = 'SUCCEEDED', claim_token = NULL, lease_until = NULL, "
                                + "last_error = NULL, updated_at = clock_timestamp() "
                                + "WHERE id = ? AND deleted = FALSE AND status = 'RUNNING' AND claim_token = ? AND lease_until > clock_timestamp()",
                        claim.event().id(),
                        claim.token())
                == 1));
    }

    public boolean fail(Claim claim, Exception failure) {
        long delayMillis = Math.min(
                86400000L,
                retryMillis(claim.event().id())
                        * (isExponential(claim.event().id()) ? (1L << Math.min(20, claim.attempt() - 1)) : 1L));
        String error = failure.getClass().getSimpleName();
        if (error.length() > 256) {
            error = error.substring(0, 256);
        }
        String errorCode = error;
        return Boolean.TRUE.equals(transactions.execute(status -> jdbc.update(
                        "UPDATE loadup_task SET status = ?, claim_token = NULL, lease_until = NULL, last_error = ?, "
                                + "next_execute_at = (clock_timestamp() + ? * INTERVAL '1 microsecond'), updated_at = clock_timestamp() "
                                + "WHERE id = ? AND deleted = FALSE AND status = 'RUNNING' AND claim_token = ? AND lease_until > clock_timestamp()",
                        claim.attempt() >= claim.event().maxAttempts() ? "FAILED" : "PENDING",
                        errorCode,
                        delayMillis * 1000,
                        claim.event().id(),
                        claim.token())
                == 1));
    }

    @Override
    public Optional<TaskExecution> find(String eventId) {
        TaskSubmission.required(eventId, "eventId", 64);
        return jdbc
                .query(
                        "SELECT * " + "FROM loadup_task WHERE id = ? AND deleted = FALSE",
                        JdbcTaskRepository::record,
                        eventId)
                .stream()
                .findFirst();
    }

    @Override
    public List<TaskExecution> failures(int limit) {
        if (limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("limit must be between 1 and 1000");
        }
        return jdbc.query(
                "SELECT * "
                        + "FROM loadup_task WHERE deleted = FALSE AND status = 'FAILED' ORDER BY created_at, id LIMIT ?",
                JdbcTaskRepository::record,
                limit);
    }

    @Override
    public Optional<TaskExecution> find(String type, String biz) {
        TaskSubmission.required(type, "taskType", 128);
        TaskSubmission.required(biz, "bizId", 128);
        return jdbc
                .query(
                        "SELECT * FROM loadup_task WHERE task_type = ? AND biz_id = ? AND deleted = FALSE ORDER BY created_at DESC,id DESC LIMIT 1",
                        JdbcTaskRepository::record,
                        type,
                        biz)
                .stream()
                .findFirst();
    }

    private TransactionTemplate management() {
        TransactionTemplate template = new TransactionTemplate(transactions.getTransactionManager());
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return template;
    }

    @Override
    public boolean cancel(String id) {
        TaskSubmission.required(id, "id", 64);
        return Boolean.TRUE.equals(management().execute(tx -> jdbc.update("""
            UPDATE loadup_task SET status='CANCELLED',claim_token=NULL,lease_until=NULL,updated_at=clock_timestamp()
            WHERE id=? AND deleted=FALSE AND status IN ('PENDING','RUNNING','FAILED')
            """, id) == 1));
    }

    @Override
    public void reschedule(String id, java.time.LocalDateTime time) {
        TaskSubmission.required(id, "id", 64);
        if (time == null) throw new IllegalArgumentException("executeAt is required");
        var deadline = time.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        management().executeWithoutResult(tx -> {
            if (jdbc.update("""
                UPDATE loadup_task SET status='PENDING',execute_at=?,next_execute_at=?,attempts=0,
                    claim_token=NULL,lease_until=NULL,last_error=NULL,updated_at=clock_timestamp()
                WHERE id=? AND deleted=FALSE AND status IN ('PENDING','FAILED','CANCELLED')
                """, deadline, deadline, id) != 1)
                throw new io.github.loadup.commons.error.CommonException(TaskResultCodeEnum.INVALID_STATE);
        });
    }

    @Override
    public void retryFailed(String id, String operator, String reason) {
        TaskSubmission.required(id, "id", 64);
        TaskSubmission.required(operator, "operator", 64);
        TaskSubmission.required(reason, "reason", 512);
        management().executeWithoutResult(tx -> {
            if (jdbc.update("""
                UPDATE loadup_task SET status='PENDING',next_execute_at=clock_timestamp(),attempts=0,
                    claim_token=NULL,lease_until=NULL,last_error=NULL,updated_at=clock_timestamp()
                WHERE id=? AND deleted=FALSE AND status='FAILED'
                """, id) != 1)
                throw new io.github.loadup.commons.error.CommonException(TaskResultCodeEnum.INVALID_STATE);
            jdbc.update(
                    "INSERT INTO loadup_task_operation(id,created_at,updated_at,task_id,operator_id,reason) VALUES (?,clock_timestamp(),clock_timestamp(),?,?,?)",
                    UUID.randomUUID().toString(),
                    id,
                    operator,
                    reason);
        });
    }

    private boolean isExponential(String id) {
        return Boolean.TRUE.equals(
                jdbc.queryForObject("SELECT exponential FROM loadup_task WHERE id=?", Boolean.class, id));
    }

    private long retryMillis(String id) {
        return jdbc.queryForObject("SELECT retry_delay_ms FROM loadup_task WHERE id=?", Long.class, id);
    }

    private long leaseMillis(String id) {
        return jdbc.queryForObject("SELECT lease_ms FROM loadup_task WHERE id=?", Long.class, id);
    }

    public long pendingCount() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM loadup_task WHERE deleted = FALSE AND status IN ('PENDING', 'RUNNING')",
                Long.class);
        return count == null ? 0 : count;
    }

    public long failedCount() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM loadup_task WHERE deleted = FALSE AND status = 'FAILED'", Long.class);
        return count == null ? 0 : count;
    }

    public long oldestPendingSeconds() {
        Long age = jdbc.queryForObject(
                "SELECT COALESCE(CAST(EXTRACT(EPOCH FROM (clock_timestamp() - MIN(created_at))) AS BIGINT), 0) "
                        + "FROM loadup_task WHERE deleted = FALSE AND status IN ('PENDING', 'RUNNING')",
                Long.class);
        return age == null ? 0 : Math.max(0, age);
    }

    void withoutTransaction(Runnable work) {
        TransactionTemplate outside = new TransactionTemplate(transactions.getTransactionManager());
        outside.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        outside.executeWithoutResult(tx -> work.run());
    }

    public record Claim(TaskExecution event, String token, int attempt) {}
}
