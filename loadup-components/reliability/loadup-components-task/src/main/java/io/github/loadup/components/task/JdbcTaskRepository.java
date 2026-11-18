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

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.task.TaskSql.Query;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

public class JdbcTaskRepository implements TaskTemplate {
    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final TransactionTemplate transactions;
    private final TaskProperties properties;
    private final TaskSql sql;

    public JdbcTaskRepository(DataSource dataSource, PlatformTransactionManager manager, TaskProperties properties) {
        this.dataSource = dataSource;
        this.jdbc = new JdbcTemplate(dataSource);
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        this.properties = properties;
        properties.validate();
        sql = new TaskSql(properties);
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
                rs.getObject("next_execute_at", LocalDateTime.class),
                TaskStatus.valueOf(rs.getString("status")),
                rs.getInt("attempts"),
                rs.getInt("max_attempts"),
                rs.getObject("lease_until", LocalDateTime.class),
                rs.getString("last_error"),
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
        jdbc.update(
                sql.get(Query.UPSERT),
                id,
                task.taskType(),
                task.bizId(),
                task.payload(),
                trace,
                task.executeAt(),
                policy.getMaxAttempts());
        // Read the locked row instead of relying on driver-specific upsert update counts.
        var rows =
                jdbc.query(sql.get(Query.LOCK_BY_BUSINESS), JdbcTaskRepository::record, task.taskType(), task.bizId());
        if (rows.isEmpty()) {
            throw new IllegalStateException("Task deduplication record is unavailable");
        }
        var existing = rows.getFirst();
        if (!Objects.equals(existing.payload(), task.payload())) {
            throw new CommonException(TaskResultCodeEnum.CONFLICT);
        }
        return existing.id();
    }

    public Optional<Claim> claim(List<String> taskTypes) {
        if (taskTypes.isEmpty()) {
            return Optional.empty();
        }
        var parameters = Map.of("taskTypes", taskTypes);
        return transactions.execute(status -> {
            namedJdbc.update(sql.get(Query.RECOVER_EXPIRED), parameters);
            var candidates = namedJdbc.query(sql.get(Query.CLAIM_CANDIDATE), parameters, JdbcTaskRepository::record);
            if (candidates.isEmpty()) {
                return Optional.empty();
            }
            var candidate = candidates.getFirst();
            String id = candidate.id();
            String token = UUID.randomUUID().toString();
            long leaseMillis =
                    properties.policy(candidate.taskType()).getLease().toMillis();
            if (jdbc.update(sql.get(Query.CLAIM), token, leaseMillis, id) != 1) {
                throw new IllegalStateException("Locked task changed during claim");
            }
            var claimed = find(id).orElseThrow();
            return Optional.of(new Claim(claimed, token, claimed.attempts()));
        });
    }

    public boolean complete(Claim claim) {
        return Boolean.TRUE.equals(transactions.execute(status -> lockOwner(claim)
                && jdbc.update(sql.get(Query.COMPLETE), claim.event().id(), claim.token()) == 1));
    }

    public boolean fail(Claim claim, Exception failure) {
        String errorType = failure.getClass().getSimpleName();
        String error = errorType.substring(0, Math.min(256, errorType.length()));
        boolean exhausted = claim.attempt() >= claim.event().maxAttempts();
        String status = exhausted ? "FAILED" : "PENDING";
        Long retryDelayMillis =
                exhausted ? null : properties.policy(claim.event().taskType()).retryDelayMillis(claim.attempt());
        return Boolean.TRUE.equals(transactions.execute(tx -> lockOwner(claim)
                && jdbc.update(
                                sql.get(Query.FAIL),
                                status,
                                error,
                                retryDelayMillis,
                                claim.event().id(),
                                claim.token())
                        == 1));
    }

    private boolean lockOwner(Claim claim) {
        // MySQL's current timestamp is fixed at statement start. Check the lease after acquiring the lock.
        return !jdbc.queryForList(
                        sql.get(Query.LOCK_OWNER), String.class, claim.event().id(), claim.token())
                .isEmpty();
    }

    @Override
    public Optional<TaskExecution> find(String id) {
        TaskSubmission.required(id, "id", 64);
        return jdbc.query(sql.get(Query.FIND_BY_ID), JdbcTaskRepository::record, id).stream()
                .findFirst();
    }

    @Override
    public List<TaskExecution> failures(int limit) {
        if (limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("limit must be between 1 and 1000");
        }
        return jdbc.query(sql.get(Query.FIND_FAILURES), JdbcTaskRepository::record, limit);
    }

    @Override
    public Optional<TaskExecution> find(String type, String biz) {
        TaskSubmission.required(type, "taskType", 128);
        TaskSubmission.required(biz, "bizId", 128);
        return jdbc.query(sql.get(Query.FIND_BY_BUSINESS), JdbcTaskRepository::record, type, biz).stream()
                .findFirst();
    }

    private TransactionTemplate management() {
        return new TransactionTemplate(transactions.getTransactionManager());
    }

    @Override
    public boolean cancel(String id) {
        TaskSubmission.required(id, "id", 64);
        return Boolean.TRUE.equals(management().execute(tx -> jdbc.update(sql.get(Query.CANCEL), id) == 1));
    }

    @Override
    public void reschedule(String id, LocalDateTime time) {
        TaskSubmission.required(id, "id", 64);
        if (time == null) {
            throw new IllegalArgumentException("executeAt is required");
        }
        var deadline = time.truncatedTo(ChronoUnit.MICROS);
        management().executeWithoutResult(tx -> {
            if (jdbc.update(sql.get(Query.RESCHEDULE), deadline, id) != 1) {
                throw new CommonException(TaskResultCodeEnum.INVALID_STATE);
            }
        });
    }

    @Override
    public void retryFailed(String id, String operator, String reason) {
        TaskSubmission.required(id, "id", 64);
        TaskSubmission.required(operator, "operator", 64);
        TaskSubmission.required(reason, "reason", 512);
        management().executeWithoutResult(tx -> {
            if (jdbc.update(sql.get(Query.RETRY_FAILED), id) != 1) {
                throw new CommonException(TaskResultCodeEnum.INVALID_STATE);
            }
            jdbc.update(sql.get(Query.INSERT_OPERATION), UUID.randomUUID().toString(), id, operator, reason);
        });
    }

    public long pendingCount() {
        return jdbc.queryForObject(sql.get(Query.COUNT_PENDING), Long.class);
    }

    public long failedCount() {
        return jdbc.queryForObject(sql.get(Query.COUNT_FAILED), Long.class);
    }

    public long oldestPendingSeconds() {
        return Math.max(0, jdbc.queryForObject(sql.get(Query.OLDEST_PENDING), Long.class));
    }

    void withoutTransaction(Runnable work) {
        TransactionTemplate outside = new TransactionTemplate(transactions.getTransactionManager());
        outside.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        outside.executeWithoutResult(_ -> work.run());
    }

    public record Claim(TaskExecution event, String token, int attempt) {}
}
