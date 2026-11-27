/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.scheduler;

import java.util.List;

public interface SchedulerProvider {
    /**
     * Apply the validated schedules and route firings back to the application. Implementations must
     * release local resources if startup fails.
     */
    void start(List<TaskSchedule> schedules, TaskTrigger trigger);

    void stop();

    @FunctionalInterface
    interface TaskTrigger {
        void trigger(String name) throws Exception;
    }
}
