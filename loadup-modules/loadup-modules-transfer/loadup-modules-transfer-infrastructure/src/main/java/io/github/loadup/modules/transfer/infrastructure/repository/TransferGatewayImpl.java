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

import static io.github.loadup.modules.transfer.infrastructure.dataobject.table.Tables.*;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import com.mybatisflex.core.util.UpdateEntity;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import io.github.loadup.modules.transfer.domain.model.*;
import io.github.loadup.modules.transfer.infrastructure.converter.TransferStorageConverter;
import io.github.loadup.modules.transfer.infrastructure.dataobject.*;
import io.github.loadup.modules.transfer.infrastructure.mapper.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

public class TransferGatewayImpl implements TransferGateway {

    private final TransferTaskDOMapper tasks;
    private final TransferTaskOptionDOMapper options;
    private final TransferStorageConverter converter;

    public TransferGatewayImpl(
            TransferTaskDOMapper tasks, TransferTaskOptionDOMapper options, TransferStorageConverter converter) {
        this.tasks = tasks;
        this.options = options;
        this.converter = converter;
    }

    private QueryWrapper task(String id) {
        return QueryWrapper.create().from(TRANSFER_TASK_DO).where(TRANSFER_TASK_DO.ID.eq(id));
    }

    @Override
    @Transactional
    public void insert(TransferTask task, Map<String, String> values) {
        tasks.insert(converter.toDO(task));
        values.forEach((key, value) -> {
            var entity = new TransferTaskOptionDO();
            entity.setTaskId(task.id());
            entity.setTenantId(task.tenantId());
            entity.setOptionName(key);
            entity.setOptionValue(value);
            options.insert(entity);
        });
    }
    /** Job scheduler bootstrap lookup; user-facing services verify tenant and ownership. */
    @Override
    public Optional<TransferTask> find(String id) {
        return TenantManager.withoutTenantCondition(
                () -> Optional.ofNullable(tasks.selectOneByQuery(task(id))).map(converter::toDomain));
    }

    @Override
    public TransferPage<TransferTask> list(String tenant, String owner, int page, int size) {
        var q = QueryWrapper.create()
                .from(TRANSFER_TASK_DO)
                .where(TRANSFER_TASK_DO.TENANT_ID.eq(tenant))
                .and(TRANSFER_TASK_DO.OWNER_ID.eq(owner));
        long total = tasks.selectCountByQuery(q);
        q.orderBy(TRANSFER_TASK_DO.CREATED_AT.desc(), TRANSFER_TASK_DO.ID.desc())
                .limit(size)
                .offset((long) (page - 1) * size);
        return new TransferPage<>(
                tasks.selectListByQuery(q).stream().map(converter::toDomain).toList(), total, page, size);
    }

    @Override
    public Map<String, String> options(String id) {
        Map<String, String> result = new LinkedHashMap<>();
        options.selectListByQuery(QueryWrapper.create()
                        .from(TRANSFER_TASK_OPTION_DO)
                        .where(TRANSFER_TASK_OPTION_DO.TASK_ID.eq(id))
                        .orderBy(TRANSFER_TASK_OPTION_DO.OPTION_NAME.asc()))
                .forEach(entity -> result.put(entity.getOptionName(), entity.getOptionValue()));
        return Map.copyOf(result);
    }

    @Override
    public boolean start(String id) {
        var patch = UpdateEntity.of(TransferTaskDO.class);
        patch.setStatus(TransferStatus.RUNNING);
        patch.setStartedAt(LocalDateTime.now(java.time.ZoneOffset.UTC));
        patch.setErrorMessage(null);
        return tasks.updateByQuery(
                        patch,
                        task(id).and(TRANSFER_TASK_DO.STATUS.in(
                                TransferStatus.QUEUED.name(), TransferStatus.RUNNING.name())))
                > 0;
    }

    @Override
    @Transactional
    public void progress(String id, long processed, long total) {
        var current = tasks.selectOneByQuery(task(id).and(TRANSFER_TASK_DO.STATUS.eq(TransferStatus.RUNNING.name()))
                .forUpdate());
        if (current == null) return;
        var patch = UpdateEntity.of(TransferTaskDO.class);
        patch.setProcessedCount(Math.max(current.getProcessedCount(), processed));
        patch.setTotalCount(Math.max(current.getTotalCount(), total));
        tasks.updateByQuery(patch, task(id).and(TRANSFER_TASK_DO.STATUS.eq(TransferStatus.RUNNING.name())));
    }

    @Override
    public void succeed(String id, String file) {
        var patch = UpdateEntity.of(TransferTaskDO.class);
        patch.setStatus(TransferStatus.SUCCEEDED);
        patch.setResultFileId(file);
        patch.setFinishedAt(LocalDateTime.now(java.time.ZoneOffset.UTC));
        patch.setErrorMessage(null);
        if (tasks.updateByQuery(patch, task(id).and(TRANSFER_TASK_DO.STATUS.eq(TransferStatus.RUNNING.name()))) != 1)
            throw new IllegalStateException("transfer task is no longer running");
    }

    @Override
    public void fail(String id, String message) {
        var patch = UpdateEntity.of(TransferTaskDO.class);
        patch.setStatus(TransferStatus.FAILED);
        patch.setErrorMessage(message);
        patch.setFinishedAt(LocalDateTime.now(java.time.ZoneOffset.UTC));
        tasks.updateByQuery(
                patch,
                task(id).and(TRANSFER_TASK_DO.STATUS.in(TransferStatus.QUEUED.name(), TransferStatus.RUNNING.name())));
    }

    @Override
    public boolean requeue(String id) {
        var patch = UpdateEntity.of(TransferTaskDO.class);
        patch.setStatus(TransferStatus.QUEUED);
        patch.setProcessedCount(0);
        patch.setTotalCount(0);
        patch.setResultFileId(null);
        patch.setErrorMessage(null);
        patch.setStartedAt(null);
        patch.setFinishedAt(null);
        return tasks.updateByQuery(patch, task(id).and(TRANSFER_TASK_DO.STATUS.eq(TransferStatus.FAILED.name()))) == 1;
    }
}
