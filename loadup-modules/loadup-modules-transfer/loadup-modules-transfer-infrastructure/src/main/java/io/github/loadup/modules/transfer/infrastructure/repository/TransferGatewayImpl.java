/*
 * #%L
 * LoadUp Transfer Infrastructure
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
package io.github.loadup.modules.transfer.infrastructure.repository;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.modules.transfer.client.error.TransferResultCodeEnum;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import io.github.loadup.modules.transfer.domain.model.TransferKind;
import io.github.loadup.modules.transfer.domain.model.TransferStatus;
import io.github.loadup.modules.transfer.domain.model.TransferTask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class TransferGatewayImpl implements TransferGateway {
    private final JdbcTemplate jdbc;
    private final IdGenerator ids;
    private final Clock clock;

    public TransferGatewayImpl(JdbcTemplate jdbc, IdGenerator ids, Clock clock) {
        this.jdbc = jdbc;
        this.ids = ids;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void insert(TransferTask task, Map<String, String> values) {
        LocalDateTime now = LocalDateTime.now(clock);
        int inserted = jdbc.update(
                """
                INSERT INTO transfer_task (id, tenant_id, owner_id, kind, handler_key, source_file_id, result_file_id,
                    status, processed_count, total_count, error_message, created_at, started_at, finished_at, updated_at, deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)
                """,
                task.id(),
                task.tenantId(),
                task.ownerId(),
                task.kind().name(),
                task.handlerKey(),
                task.sourceFileId(),
                task.resultFileId(),
                task.status().name(),
                task.processedCount(),
                task.totalCount(),
                task.errorMessage(),
                task.createdAt() == null ? now : task.createdAt(),
                task.startedAt(),
                task.finishedAt(),
                now);
        if (inserted != 1) throw new IllegalStateException("Unexpected transfer insert count");
        for (var entry : values.entrySet()) {
            int count = jdbc.update(
                    """
                    INSERT INTO transfer_task_option (id, tenant_id, task_id, option_name, option_value, created_at, updated_at, deleted)
                    VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)
                    """, ids.generate(), task.tenantId(), task.id(), entry.getKey(), entry.getValue(), now, now);
            if (count != 1) throw new IllegalStateException("Unexpected transfer option insert count");
        }
    }

    /** Internal cross-tenant scheduler scan; caller restores the persisted tenant before execution. */
    @Override
    public List<String> queued(int limit) {
        if (limit < 1 || limit > 100) throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        return jdbc.query(
                "SELECT id FROM transfer_task WHERE status = 'QUEUED' AND deleted = FALSE "
                        + "ORDER BY created_at ASC, id ASC LIMIT ?",
                (rs, index) -> rs.getString("id"),
                limit);
    }

    /** Internal bootstrap lookup; user-facing callers verify tenant and ownership. */
    @Override
    public Optional<TransferTask> find(String id) {
        return jdbc
                .query("SELECT * FROM transfer_task WHERE id = ? AND deleted = FALSE", TransferGatewayImpl::task, id)
                .stream()
                .findFirst();
    }

    @Override
    public PageResult<TransferTask> list(String tenant, String owner, int page, int size) {
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM transfer_task WHERE tenant_id = ? AND owner_id = ? " + "AND deleted = FALSE",
                Long.class,
                tenant,
                owner);
        var rows = jdbc.query(
                "SELECT * FROM transfer_task WHERE tenant_id = ? AND owner_id = ? AND deleted = FALSE "
                        + "ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                TransferGatewayImpl::task,
                tenant,
                owner,
                size,
                (long) (page - 1) * size);
        return new PageResult<>(rows, total == null ? 0 : total, page, size);
    }

    @Override
    public Map<String, String> options(String tenant, String id) {
        Map<String, String> result = new LinkedHashMap<>();
        jdbc.query(
                "SELECT option_name, option_value FROM transfer_task_option WHERE tenant_id = ? AND task_id = ? "
                        + "AND deleted = FALSE ORDER BY option_name ASC",
                rs -> {
                    result.put(rs.getString("option_name"), rs.getString("option_value"));
                },
                tenant,
                id);
        return Map.copyOf(result);
    }

    @Override
    public boolean start(String tenant, String id) {
        LocalDateTime now = LocalDateTime.now(clock);
        return jdbc.update(
                        "UPDATE transfer_task SET status = 'RUNNING', started_at = ?, error_message = NULL, updated_at = ? "
                                + "WHERE tenant_id = ? AND id = ? AND status = 'QUEUED' AND deleted = FALSE",
                        now,
                        now,
                        tenant,
                        id)
                == 1;
    }

    @Override
    public void progress(String tenant, String id, long processed, long total) {
        // A task that has left RUNNING ignores stale progress reports.
        jdbc.update(
                "UPDATE transfer_task SET processed_count = GREATEST(processed_count, ?), "
                        + "total_count = GREATEST(total_count, ?), updated_at = ? "
                        + "WHERE tenant_id = ? AND id = ? AND status = 'RUNNING' AND deleted = FALSE",
                processed,
                total,
                LocalDateTime.now(clock),
                tenant,
                id);
    }

    @Override
    public void succeed(String tenant, String id, String file) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (jdbc.update(
                        "UPDATE transfer_task SET status = 'SUCCEEDED', result_file_id = ?, finished_at = ?, "
                                + "error_message = NULL, updated_at = ? WHERE tenant_id = ? AND id = ? AND status = 'RUNNING' "
                                + "AND deleted = FALSE",
                        file,
                        now,
                        now,
                        tenant,
                        id)
                != 1) throw new CommonException(TransferResultCodeEnum.TRANSFER_TASK_IS_NO_LONGER_RUNNING);
    }

    @Override
    public void fail(String tenant, String id, String message) {
        LocalDateTime now = LocalDateTime.now(clock);
        // A terminal task is not overwritten by a stale failure.
        jdbc.update(
                "UPDATE transfer_task SET status = 'FAILED', error_message = ?, finished_at = ?, updated_at = ? "
                        + "WHERE tenant_id = ? AND id = ? AND status IN ('QUEUED', 'RUNNING') AND deleted = FALSE",
                message,
                now,
                now,
                tenant,
                id);
    }

    @Override
    public boolean requeue(String tenant, String id) {
        return jdbc.update(
                        "UPDATE transfer_task SET status = 'QUEUED', processed_count = 0, total_count = 0, "
                                + "result_file_id = NULL, error_message = NULL, started_at = NULL, finished_at = NULL, updated_at = ? "
                                + "WHERE tenant_id = ? AND id = ? AND status = 'FAILED' AND deleted = FALSE",
                        LocalDateTime.now(clock),
                        tenant,
                        id)
                == 1;
    }

    private static TransferTask task(ResultSet rs, int index) throws SQLException {
        return new TransferTask(
                rs.getString("id"),
                rs.getString("tenant_id"),
                rs.getString("owner_id"),
                TransferKind.valueOf(rs.getString("kind")),
                rs.getString("handler_key"),
                rs.getString("source_file_id"),
                rs.getString("result_file_id"),
                TransferStatus.valueOf(rs.getString("status")),
                rs.getLong("processed_count"),
                rs.getLong("total_count"),
                rs.getString("error_message"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("started_at", LocalDateTime.class),
                rs.getObject("finished_at", LocalDateTime.class));
    }
}
