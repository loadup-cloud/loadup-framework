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
package io.github.loadup.components.scheduler;

import io.github.loadup.components.scheduler.model.ScheduleRequest;
import io.github.loadup.components.scheduler.model.SchedulerStatus;
import java.util.Optional;

public interface SchedulerTemplate {

    /**
     * Registers or updates a recurring task. The underlying engine executes the registered
     * {@link SchedulerProcessor} according to the cron expression.
     *
     * @param request the schedule to register
     */
    void register(ScheduleRequest request);

    /**
     * Deletes the recurring task identified by {@code taskName}. Deleting an unknown task is a
     * no-op.
     *
     * @param taskName the task name
     */
    void delete(String taskName);

    /**
     * Triggers one immediate execution of the recurring task identified by {@code taskName},
     * without changing its schedule. Unknown tasks are a no-op.
     *
     * @param taskName the task name
     */
    void trigger(String taskName);

    /**
     * Updates the cron expression of an existing task. Unknown tasks are a no-op.
     *
     * @param taskName the task name
     * @param cron the new cron expression
     */
    void updateCron(String taskName, String cron);

    /**
     * Returns the current status of the recurring task identified by {@code taskName}.
     *
     * @param taskName the task name
     * @return the status, or empty when no task is registered under this name
     */
    Optional<SchedulerStatus> getStatus(String taskName);
}
