/*
 * #%L
 * Loadup Scheduler Api
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
package io.github.loadup.components.scheduler.model;

import java.util.Map;
import java.util.Objects;

public record ScheduleRequest(String taskName, String cron, Map<String, String> args, String zoneId) {

    public ScheduleRequest {
        Objects.requireNonNull(taskName, "taskName must not be null");
        Objects.requireNonNull(cron, "cron must not be null");
        args = args == null ? Map.of() : Map.copyOf(args);
    }

    /**
     * Creates a request with the system timezone.
     *
     * @param taskName the task name
     * @param cron the cron expression
     * @return the request
     */
    public static ScheduleRequest of(String taskName, String cron) {
        return new ScheduleRequest(taskName, cron, null, null);
    }

    /**
     * Creates a request with a payload and the system timezone.
     *
     * @param taskName the task name
     * @param cron the cron expression
     * @param args the payload
     * @return the request
     */
    public static ScheduleRequest of(String taskName, String cron, Map<String, String> args) {
        return new ScheduleRequest(taskName, cron, args, null);
    }
}
