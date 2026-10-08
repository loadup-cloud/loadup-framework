/*
 * #%L
 * LoadUp Outbox
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
package io.github.loadup.components.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/** MySQL 8 durable queue. Claim and acknowledgement transactions never enclose handlers. */
public class JdbcOutboxRepository implements OutboxTemplate {
    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final OutboxProperties properties;

    public JdbcOutboxRepository(DataSource dataSource, OutboxProperties properties) {
        this.dataSource = dataSource;
        this.jdbc = new JdbcTemplate(dataSource);
        this.properties = properties;
        properties.validate();
        transactions = new TransactionTemplate(new JdbcTransactionManager(dataSource));
        transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    static void requireTransaction(DataSource dataSource) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || !TransactionSynchronizationManager.hasResource(dataSource)) {
            throw new IllegalStateException("Outbox requires a writable transaction on its DataSource");
        }
        var connection = DataSourceUtils.getConnection(dataSource);
        try {
            if (connection.getAutoCommit())
                throw new IllegalStateException("Outbox DataSource is not participating in the transaction");
        } catch (SQLException failure) {
            throw new IllegalStateException("Cannot inspect outbox transaction", failure);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public String publish(OutboxMessage message) {
        requireTransaction(dataSource);
        if (message == null) throw new IllegalArgumentException("message is required");
        String id = UUID.randomUUID().toString();
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.matches("[a-fA-F0-9]{16,32}")) traceId = null;
        jdbc.update(
                "INSERT INTO loadup_outbox_event (id, tenant_id, created_at, updated_at, event_type, "
                        + "payload_version, business_id, payload, trace_id, status, available_at) "
                        + "VALUES (?, ?, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), ?, ?, ?, ?, ?, 'PENDING', UTC_TIMESTAMP(6))",
                id,
                message.tenantId(),
                message.type(),
                message.version(),
                message.businessId(),
                message.payload(),
                traceId);
        return id;
    }

    Optional<Claim> claim() {
        return transactions.execute(status -> {
            List<Claim> rows = jdbc.query(
                    "SELECT * FROM loadup_outbox_event WHERE "
                            + "(status = 'PENDING' AND available_at <= UTC_TIMESTAMP(6)) OR "
                            + "(status = 'PROCESSING' AND lease_until <= UTC_TIMESTAMP(6)) "
                            + "ORDER BY created_at, id LIMIT 1 FOR UPDATE SKIP LOCKED",
                    (rs, row) -> new Claim(event(rs), UUID.randomUUID().toString(), rs.getInt("attempts") + 1));
            if (rows.isEmpty()) return Optional.empty();
            Claim claim = rows.getFirst();
            if (claim.attempt() > properties.getMaxAttempts()) {
                jdbc.update(
                        "UPDATE loadup_outbox_event SET status = 'FAILED', claim_token = NULL, lease_until = NULL, "
                                + "last_error = 'LeaseExpired', updated_at = UTC_TIMESTAMP(6) WHERE id = ?",
                        claim.event().id());
                return Optional.empty();
            }
            jdbc.update(
                    "UPDATE loadup_outbox_event SET status = 'PROCESSING', attempts = ?, claim_token = ?, "
                            + "lease_until = TIMESTAMPADD(MICROSECOND, ?, UTC_TIMESTAMP(6)), updated_at = UTC_TIMESTAMP(6) "
                            + "WHERE id = ?",
                    claim.attempt(),
                    claim.token(),
                    properties.getLease().toMillis() * 1000,
                    claim.event().id());
            return Optional.of(claim);
        });
    }

    boolean complete(Claim claim) {
        return Boolean.TRUE.equals(transactions.execute(status -> jdbc.update(
                        "UPDATE loadup_outbox_event SET status = 'SUCCEEDED', claim_token = NULL, lease_until = NULL, "
                                + "last_error = NULL, updated_at = UTC_TIMESTAMP(6) "
                                + "WHERE id = ? AND status = 'PROCESSING' AND claim_token = ? AND lease_until > UTC_TIMESTAMP(6)",
                        claim.event().id(),
                        claim.token())
                == 1));
    }

    void fail(Claim claim, Exception failure) {
        long delayMillis =
                Math.min(86400000L, properties.getRetryDelay().toMillis() * (1L << Math.min(20, claim.attempt() - 1)));
        String error = failure.getClass().getSimpleName();
        if (error.length() > 256) error = error.substring(0, 256);
        String errorCode = error;
        transactions.executeWithoutResult(status -> jdbc.update(
                "UPDATE loadup_outbox_event SET status = ?, claim_token = NULL, lease_until = NULL, last_error = ?, "
                        + "available_at = TIMESTAMPADD(MICROSECOND, ?, UTC_TIMESTAMP(6)), updated_at = UTC_TIMESTAMP(6) "
                        + "WHERE id = ? AND status = 'PROCESSING' AND claim_token = ? AND lease_until > UTC_TIMESTAMP(6)",
                claim.attempt() >= properties.getMaxAttempts() ? "FAILED" : "PENDING",
                errorCode,
                delayMillis * 1000,
                claim.event().id(),
                claim.token()));
    }

    @Override
    public Optional<OutboxRecord> find(String tenantId, String eventId) {
        OutboxMessage.required(tenantId, "tenantId", 64);
        OutboxMessage.required(eventId, "eventId", 64);
        return jdbc
                .query(
                        "SELECT id, tenant_id, event_type, business_id, status, attempts, last_error "
                                + "FROM loadup_outbox_event WHERE tenant_id = ? AND id = ?",
                        JdbcOutboxRepository::record,
                        tenantId,
                        eventId)
                .stream()
                .findFirst();
    }

    @Override
    public List<OutboxRecord> failures(String tenantId, int limit) {
        OutboxMessage.required(tenantId, "tenantId", 64);
        if (limit < 1 || limit > 1000) throw new IllegalArgumentException("limit must be between 1 and 1000");
        return jdbc.query(
                "SELECT id, tenant_id, event_type, business_id, status, attempts, last_error "
                        + "FROM loadup_outbox_event WHERE tenant_id = ? AND status = 'FAILED' ORDER BY created_at, id LIMIT ?",
                JdbcOutboxRepository::record,
                tenantId,
                limit);
    }

    @Override
    public boolean replay(String tenantId, String eventId, String operatorId, String reason) {
        OutboxMessage.required(tenantId, "tenantId", 64);
        OutboxMessage.required(eventId, "eventId", 64);
        OutboxMessage.required(operatorId, "operatorId", 64);
        OutboxMessage.required(reason, "reason", 512);
        return Boolean.TRUE.equals(transactions.execute(status -> {
            int changed = jdbc.update(
                    "UPDATE loadup_outbox_event SET status = 'PENDING', attempts = 0, last_error = NULL, "
                            + "available_at = UTC_TIMESTAMP(6), updated_at = UTC_TIMESTAMP(6) "
                            + "WHERE tenant_id = ? AND id = ? AND status = 'FAILED'",
                    tenantId,
                    eventId);
            if (changed == 0) return false;
            jdbc.update(
                    "INSERT INTO loadup_outbox_replay (id, tenant_id, created_at, updated_at, event_id, operator_id, reason) "
                            + "VALUES (?, ?, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), ?, ?, ?)",
                    UUID.randomUUID().toString(),
                    tenantId,
                    eventId,
                    operatorId,
                    reason);
            return true;
        }));
    }

    public long pendingCount() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM loadup_outbox_event WHERE status IN ('PENDING', 'PROCESSING')", Long.class);
        return count == null ? 0 : count;
    }

    public long failedCount() {
        Long count =
                jdbc.queryForObject("SELECT COUNT(*) FROM loadup_outbox_event WHERE status = 'FAILED'", Long.class);
        return count == null ? 0 : count;
    }

    public long oldestPendingSeconds() {
        Long age = jdbc.queryForObject(
                "SELECT COALESCE(TIMESTAMPDIFF(SECOND, MIN(created_at), UTC_TIMESTAMP(6)), 0) "
                        + "FROM loadup_outbox_event WHERE status IN ('PENDING', 'PROCESSING')",
                Long.class);
        return age == null ? 0 : Math.max(0, age);
    }

    private static OutboxEvent event(ResultSet rs) throws SQLException {
        return new OutboxEvent(
                rs.getString("id"),
                new OutboxMessage(
                        rs.getString("tenant_id"),
                        rs.getString("event_type"),
                        rs.getInt("payload_version"),
                        rs.getString("business_id"),
                        rs.getString("payload")),
                rs.getObject("created_at", LocalDateTime.class).toInstant(ZoneOffset.UTC),
                rs.getString("trace_id"));
    }

    private static OutboxRecord record(ResultSet rs, int row) throws SQLException {
        return new OutboxRecord(
                rs.getString("id"),
                rs.getString("tenant_id"),
                rs.getString("event_type"),
                rs.getString("business_id"),
                rs.getString("status"),
                rs.getInt("attempts"),
                rs.getString("last_error"));
    }

    record Claim(OutboxEvent event, String token, int attempt) {}
}
