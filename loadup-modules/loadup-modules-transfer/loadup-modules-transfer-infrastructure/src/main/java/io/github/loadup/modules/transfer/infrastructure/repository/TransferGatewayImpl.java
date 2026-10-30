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

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.modules.transfer.client.error.TransferResultCodeEnum;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import io.github.loadup.modules.transfer.domain.model.TransferKind;
import io.github.loadup.modules.transfer.domain.model.TransferStatus;
import io.github.loadup.modules.transfer.domain.model.TransferTask;
import io.github.loadup.modules.transfer.infrastructure.dataobject.TransferTaskRow;
import io.github.loadup.modules.transfer.infrastructure.mapper.TransferTaskOptionRowMapper;
import io.github.loadup.modules.transfer.infrastructure.mapper.TransferTaskRowMapper;
import io.github.loadup.modules.transfer.infrastructure.table.Tables;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class TransferGatewayImpl extends ServiceImpl<TransferTaskRowMapper, TransferTaskRow>
        implements TransferGateway {
    private final JdbcTemplate jdbc;
    private final TransferTaskOptionRowMapper optionMapper;
    private final IdGenerator ids;
    private final Clock clock;

    public TransferGatewayImpl(
            JdbcTemplate jdbc,
            TransferTaskRowMapper mapper,
            TransferTaskOptionRowMapper optionMapper,
            IdGenerator ids,
            Clock clock) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.optionMapper = optionMapper;
        this.ids = ids;
        this.clock = clock;
    }

    private static TransferTask task(TransferTaskRow row) {
        return new TransferTask(
                row.getId(),
                row.getOwnerId(),
                TransferKind.valueOf(row.getKind()),
                row.getHandlerKey(),
                row.getSourceFileId(),
                row.getResultFileId(),
                TransferStatus.valueOf(row.getStatus()),
                row.getProcessedCount(),
                row.getTotalCount(),
                row.getErrorMessage(),
                row.getCreatedAt(),
                row.getStartedAt(),
                row.getFinishedAt());
    }

    @Override
    @Transactional
    public void insert(TransferTask task, Map<String, String> values) {
        LocalDateTime now = LocalDateTime.now(clock);
        int inserted = jdbc.update(
                """
                INSERT INTO transfer_task (id, owner_id, kind, handler_key, source_file_id, result_file_id,
                    status, processed_count, total_count, error_message, created_at, started_at, finished_at, updated_at, deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)
                """,
                task.id(),
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
        if (inserted != 1) {
            throw new IllegalStateException("Unexpected transfer insert count");
        }
        for (var entry : values.entrySet()) {
            int count = jdbc.update("""
                INSERT INTO transfer_task_option (id, task_id, option_name, option_value, created_at, updated_at, deleted)
                VALUES (?, ?, ?, ?, ?, ?, FALSE)
                """, ids.generate(), task.id(), entry.getKey(), entry.getValue(), now, now);
            if (count != 1) {
                throw new IllegalStateException("Unexpected transfer option insert count");
            }
        }
    }

    /**
     * Scheduler scan for the current tenant schema.
     */
    @Override
    public List<String> queued(int limit) {
        if (limit < 1 || limit > 100) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        var query = QueryWrapper.create()
                .where(Tables.TRANSFER_TASK_ROW.STATUS.eq("QUEUED"))
                .orderBy(Tables.TRANSFER_TASK_ROW.CREATED_AT.asc(), Tables.TRANSFER_TASK_ROW.ID.asc())
                .limit(limit);
        return list(query).stream().map(TransferTaskRow::getId).toList();
    }

    /**
     * Internal bootstrap lookup; user-facing callers verify ownership.
     */
    @Override
    public Optional<TransferTask> find(String id) {
        return getByIdOpt(id).map(TransferGatewayImpl::task);
    }

    @Override
    public PageResult<TransferTask> list(String owner, int page, int size) {
        var result = page(
                new Page<>(page, size),
                QueryWrapper.create()
                        .where(Tables.TRANSFER_TASK_ROW.OWNER_ID.eq(owner))
                        .orderBy(Tables.TRANSFER_TASK_ROW.CREATED_AT.desc(), Tables.TRANSFER_TASK_ROW.ID.desc()));
        return new PageResult<>(
                result.getRecords().stream().map(TransferGatewayImpl::task).toList(), result.getTotalRow(), page, size);
    }

    @Override
    public Map<String, String> options(String id) {
        Map<String, String> result = new LinkedHashMap<>();
        var query = QueryWrapper.create()
                .where(Tables.TRANSFER_TASK_OPTION_ROW.TASK_ID.eq(id))
                .orderBy(Tables.TRANSFER_TASK_OPTION_ROW.OPTION_NAME.asc());
        for (var option : optionMapper.selectListByQuery(query)) {
            result.put(option.getOptionName(), option.getOptionValue());
        }
        return Map.copyOf(result);
    }

    @Override
    public boolean start(String id) {
        LocalDateTime now = LocalDateTime.now(clock);
        return jdbc.update(
                        "UPDATE transfer_task SET status = 'RUNNING', started_at = ?, error_message = NULL, updated_at = ? "
                                + "WHERE id = ? AND status = 'QUEUED' AND deleted = FALSE",
                        now,
                        now,
                        id)
                == 1;
    }

    @Override
    public void progress(String id, long processed, long total) {
        // A task that has left RUNNING ignores stale progress reports.
        jdbc.update(
                "UPDATE transfer_task SET processed_count = GREATEST(processed_count, ?), "
                        + "total_count = GREATEST(total_count, ?), updated_at = ? "
                        + "WHERE id = ? AND status = 'RUNNING' AND deleted = FALSE",
                processed,
                total,
                LocalDateTime.now(clock),
                id);
    }

    @Override
    public void succeed(String id, String file) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (jdbc.update(
                        "UPDATE transfer_task SET status = 'SUCCEEDED', result_file_id = ?, finished_at = ?, "
                                + "error_message = NULL, updated_at = ? WHERE id = ? AND status = 'RUNNING' "
                                + "AND deleted = FALSE",
                        file,
                        now,
                        now,
                        id)
                != 1) {
            throw new CommonException(TransferResultCodeEnum.TRANSFER_TASK_IS_NO_LONGER_RUNNING);
        }
    }

    @Override
    public void fail(String id, String message) {
        LocalDateTime now = LocalDateTime.now(clock);
        // A terminal task is not overwritten by a stale failure.
        jdbc.update(
                "UPDATE transfer_task SET status = 'FAILED', error_message = ?, finished_at = ?, updated_at = ? "
                        + "WHERE id = ? AND status IN ('QUEUED', 'RUNNING') AND deleted = FALSE",
                message,
                now,
                now,
                id);
    }

    @Override
    public boolean requeue(String id) {
        return jdbc.update(
                        "UPDATE transfer_task SET status = 'QUEUED', processed_count = 0, total_count = 0, "
                                + "result_file_id = NULL, error_message = NULL, started_at = NULL, finished_at = NULL, updated_at = ? "
                                + "WHERE id = ? AND status = 'FAILED' AND deleted = FALSE",
                        LocalDateTime.now(clock),
                        id)
                == 1;
    }
}
