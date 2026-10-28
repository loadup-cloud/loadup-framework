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

import com.mybatisflex.core.tenant.TenantManager;
import io.github.loadup.commons.context.*;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.database.transaction.JdbcSavepointExecutor;
import javax.sql.DataSource;
import io.github.loadup.components.delayedtask.dataobject.DelayedTaskDO;
import java.time.*;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Internal business API: call from trusted tenant scopes and a transaction shared with business data. */
public class DelayedTaskTemplate {
    private final DelayedTaskStore store;
    private final DelayedTaskConverter converter;
    private final DelayedTaskProperties properties;
    private final DatabaseProperties database;
    private final Clock clock;
    private final DataSource dataSource;

    public DelayedTaskTemplate(
            DelayedTaskStore store,
            DelayedTaskConverter converter,
            DelayedTaskProperties properties,
            DatabaseProperties database,
            Clock clock, DataSource dataSource) {
        this.store = store;
        this.converter = converter;
        this.properties = properties;
        this.database = database;
        this.clock = clock;
        this.dataSource = dataSource;
    }

    public DelayedTask register(String taskType, String bizId, LocalDateTime executeAt) {
        validate(taskType, bizId);
        if (executeAt == null) invalid();
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new CommonException(DelayedTaskResultCodeEnum.TRANSACTION_REQUIRED);
        String tenant = tenant();
        final LocalDateTime deadline = executeAt.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        return TenantManager.withoutTenantCondition(() -> {
            var row = new DelayedTaskDO();
            row.setTaskType(taskType);
            row.setBizId(bizId);
            row.setExecuteAt(deadline);
            row.setNextExecuteAt(deadline);
            row.setStatus(DelayedTaskStatus.PENDING);
            row.setAttempts(0);
            row.setMaxAttempts(properties.getMaxAttempts());
            row.setTenantId(tenant);
            try {
                JdbcSavepointExecutor.execute(dataSource, () -> store.mapper().insert(row));
                return converter.toTask(row);
            } catch (DuplicateKeyException duplicate) {
                var existing = store.mapper()
                        .selectOneByQuery(
                                DelayedTaskStore.key(tenant, taskType, bizId).forUpdate());
                if (existing == null) throw duplicate;
                if (!existing.getExecuteAt().equals(deadline))
                    throw new CommonException(DelayedTaskResultCodeEnum.CONFLICT);
                return converter.toTask(existing);
            }
        });
    }

    public Optional<DelayedTask> find(String type, String biz) {
        validate(type, biz);
        String tenant = tenant();
        return TenantManager.withoutTenantCondition(
                () -> Optional.ofNullable(store.mapper().selectOneByQuery(DelayedTaskStore.key(tenant, type, biz)))
                        .map(converter::toTask));
    }

    @Transactional
    public boolean cancel(String type, String biz) {
        validate(type, biz);
        String tenant = tenant();
        return TenantManager.withoutTenantCondition(() -> {
            var patch = com.mybatisflex.core.util.UpdateEntity.of(DelayedTaskDO.class);
            patch.setStatus(DelayedTaskStatus.CANCELLED);
            patch.setClaimToken(null);
            patch.setLeaseUntil(null);
            return store.mapper()
                            .updateByQuery(
                                    patch,
                                    DelayedTaskStore.key(tenant, type, biz)
                                            .and(io.github.loadup.components.delayedtask.dataobject.table.Tables
                                                    .DELAYED_TASK_DO
                                                    .STATUS.in("PENDING", "RUNNING", "FAILED")))
                    == 1;
        });
    }

    @Transactional
    public void reschedule(String type, String biz, LocalDateTime deadline) {
        validate(type, biz);
        if (deadline == null) invalid();
        change(type, biz, deadline.truncatedTo(java.time.temporal.ChronoUnit.MICROS), false);
    }

    @Transactional
    public void retryFailed(String type, String biz) {
        validate(type, biz);
        change(type, biz, LocalDateTime.now(clock), true);
    }

    private void change(String type, String biz, LocalDateTime deadline, boolean retry) {
        String tenant = tenant();
        TenantManager.withoutTenantCondition(() -> {
            var patch = com.mybatisflex.core.util.UpdateEntity.of(DelayedTaskDO.class);
            patch.setStatus(DelayedTaskStatus.PENDING);
            if (!retry) patch.setExecuteAt(deadline);
            patch.setNextExecuteAt(deadline);
            patch.setAttempts(0);
            patch.setLastError(null);
            patch.setClaimToken(null);
            patch.setLeaseUntil(null);
            var q = DelayedTaskStore.key(tenant, type, biz)
                    .and(io.github.loadup.components.delayedtask.dataobject.table.Tables.DELAYED_TASK_DO.STATUS.in(
                            retry ? new String[] {"FAILED"} : new String[] {"PENDING", "FAILED", "CANCELLED"}));
            if (store.mapper().updateByQuery(patch, q) != 1)
                throw new CommonException(DelayedTaskResultCodeEnum.INVALID_STATE);
            return null;
        });
    }

    private String tenant() {
        var config = database.getMultiTenant();
        String tenant = config.isEnabled() ? ContextHolder.get(ContextKeys.TENANT_ID) : config.getDefaultTenantId();
        if (tenant == null || tenant.isBlank()) {
            if (config.isRequired() && config.isEnabled()) invalid();
            tenant = config.getDefaultTenantId();
        }
        return tenant;
    }

    private static void validate(String type, String biz) {
        if (type == null
                || !type.matches("[A-Za-z][A-Za-z0-9_.-]{0,63}")
                || biz == null
                || biz.isBlank()
                || biz.length() > 128) invalid();
    }

    private static void invalid() {
        throw new CommonException(DelayedTaskResultCodeEnum.INVALID_ARGUMENT);
    }
}
