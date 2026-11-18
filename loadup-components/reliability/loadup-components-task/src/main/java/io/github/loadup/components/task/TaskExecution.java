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

import io.github.loadup.commons.json.DiagnosticHidden;
import io.github.loadup.commons.json.ToStringAsJson;
import java.time.Instant;
import java.time.LocalDateTime;

public record TaskExecution(
        String id,
        String taskType,
        String bizId,
        LocalDateTime nextExecuteAt,
        TaskStatus status,
        int attempts,
        int maxAttempts,
        LocalDateTime leaseUntil,
        String lastError,
        @DiagnosticHidden String payload,
        Instant occurredAt,
        String traceId) {
    public TaskExecution(
            String id,
            String type,
            String biz,
            LocalDateTime next,
            TaskStatus state,
            int attempts,
            int max,
            LocalDateTime lease,
            String error) {
        this(id, type, biz, next, state, attempts, max, lease, error, null, null, null);
    }

    public TaskExecution(String id, String type, String biz, String state, int attempts, String error) {
        this(id, type, biz, null, TaskStatus.valueOf(state), attempts, 0, null, error, null, null, null);
    }

    public TaskExecution(String id, TaskSubmission task, Instant at, String trace) {
        this(
                id,
                task.taskType(),
                task.bizId(),
                task.executeAt(),
                TaskStatus.RUNNING,
                1,
                1,
                null,
                null,
                task.payload(),
                at,
                trace);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
