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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record TaskSubmission(
        String taskType, String bizId, @DiagnosticHidden String payload, LocalDateTime executeAt) {
    public TaskSubmission {
        required(taskType, "taskType", 128);
        required(bizId, "bizId", 128);
        if (payload != null && payload.getBytes(StandardCharsets.UTF_8).length > 1048576) {
            throw new IllegalArgumentException("Invalid task payload");
        }
        if (executeAt != null) {
            executeAt = executeAt.truncatedTo(ChronoUnit.MICROS);
        }
    }

    public static TaskSubmission immediate(String type, String biz, String payload) {
        return new TaskSubmission(type, biz, payload, null);
    }

    public static TaskSubmission scheduled(String type, String biz, LocalDateTime time) {
        if (time == null) {
            throw new IllegalArgumentException("executeAt is required");
        }
        return new TaskSubmission(type, biz, null, time);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }

    static String required(String value, String name, int max) {
        if (value == null || value.isBlank() || value.length() > max) {
            throw new IllegalArgumentException("Invalid " + name);
        }
        return value;
    }
}
