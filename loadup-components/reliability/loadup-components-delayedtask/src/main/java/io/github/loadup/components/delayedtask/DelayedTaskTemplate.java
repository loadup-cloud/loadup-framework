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

import io.github.loadup.commons.error.CommonException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Internal business API: call from a tenant-scoped transaction shared with business data.
 */
public class DelayedTaskTemplate {
    private final DelayedTaskStore store;
    private final DelayedTaskConverter converter;
    private final DelayedTaskProperties properties;
    private final Clock clock;
    private final DataSource dataSource;

    public DelayedTaskTemplate(
            DelayedTaskStore store,
            DelayedTaskConverter converter,
            DelayedTaskProperties properties,
            Clock clock,
            DataSource dataSource) {
        this.store = store;
        this.converter = converter;
        this.properties = properties;
        this.clock = clock;
        this.dataSource = dataSource;
    }

    private static void validate(String type, String biz) {
        if (type == null
                || !type.matches("[A-Za-z][A-Za-z0-9_.-]{0,63}")
                || biz == null
                || biz.isBlank()
                || biz.length() > 128) {
            invalid();
        }
    }

    private static void invalid() {
        throw new CommonException(DelayedTaskResultCodeEnum.INVALID_ARGUMENT);
    }

    public DelayedTask register(String taskType, String bizId, LocalDateTime executeAt) {
        validate(taskType, bizId);
        if (executeAt == null) {
            invalid();
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || !TransactionSynchronizationManager.hasResource(dataSource)) {
            throw new CommonException(DelayedTaskResultCodeEnum.TRANSACTION_REQUIRED);
        }
        LocalDateTime deadline = executeAt.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        var row = store.insert(taskType, bizId, deadline, properties.getMaxAttempts(), LocalDateTime.now(clock));
        if (!row.getExecuteAt().equals(deadline)) {
            throw new CommonException(DelayedTaskResultCodeEnum.CONFLICT);
        }
        return converter.toTask(row);
    }

    public Optional<DelayedTask> find(String type, String biz) {
        validate(type, biz);
        return store.find(type, biz, false).map(converter::toTask);
    }

    @Transactional
    public boolean cancel(String type, String biz) {
        validate(type, biz);
        return store.cancel(type, biz, LocalDateTime.now(clock));
    }

    @Transactional
    public void reschedule(String type, String biz, LocalDateTime deadline) {
        validate(type, biz);
        if (deadline == null) {
            invalid();
        }
        change(type, biz, deadline.truncatedTo(java.time.temporal.ChronoUnit.MICROS), false);
    }

    @Transactional
    public void retryFailed(String type, String biz) {
        validate(type, biz);
        change(type, biz, LocalDateTime.now(clock), true);
    }

    private void change(String type, String biz, LocalDateTime deadline, boolean retry) {
        if (!store.reschedule(type, biz, deadline, retry, LocalDateTime.now(clock))) {
            throw new CommonException(DelayedTaskResultCodeEnum.INVALID_STATE);
        }
    }
}
