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

import io.github.loadup.commons.context.*;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.config.DatabaseProperties;
import java.time.*;
import java.util.Optional;
import javax.sql.DataSource;
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
            Clock clock,
            DataSource dataSource) {
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
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || !TransactionSynchronizationManager.hasResource(dataSource))
            throw new CommonException(DelayedTaskResultCodeEnum.TRANSACTION_REQUIRED);
        String tenant = tenant();
        LocalDateTime deadline = executeAt.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        var row =
                store.insert(tenant, taskType, bizId, deadline, properties.getMaxAttempts(), LocalDateTime.now(clock));
        if (!row.getExecuteAt().equals(deadline)) throw new CommonException(DelayedTaskResultCodeEnum.CONFLICT);
        return converter.toTask(row);
    }

    public Optional<DelayedTask> find(String type, String biz) {
        validate(type, biz);
        return store.find(tenant(), type, biz, false).map(converter::toTask);
    }

    @Transactional
    public boolean cancel(String type, String biz) {
        validate(type, biz);
        return store.cancel(tenant(), type, biz, LocalDateTime.now(clock));
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
        if (!store.reschedule(tenant(), type, biz, deadline, retry, LocalDateTime.now(clock)))
            throw new CommonException(DelayedTaskResultCodeEnum.INVALID_STATE);
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
