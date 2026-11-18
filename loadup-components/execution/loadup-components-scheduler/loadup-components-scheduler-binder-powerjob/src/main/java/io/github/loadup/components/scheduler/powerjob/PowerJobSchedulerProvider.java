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
package io.github.loadup.components.scheduler.powerjob;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.TaskSchedule;
import java.util.List;
import tech.powerjob.client.PowerJobClient;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

public class PowerJobSchedulerProvider implements SchedulerProvider, BasicProcessor {
    static final String PROCESSOR_NAME = "loadupSchedulerProcessor";

    private final PowerJobScheduleRegistry registry;
    private volatile TaskTrigger trigger;

    public PowerJobSchedulerProvider(PowerJobClient client) {
        registry = new PowerJobScheduleRegistry(client);
    }

    @Override
    public void start(List<TaskSchedule> schedules, TaskTrigger trigger) {
        registry.synchronize(schedules);
        this.trigger = trigger;
    }

    @Override
    public void stop() {
        trigger = null;
    }

    @Override
    public ProcessResult process(TaskContext task) throws Exception {
        var current = trigger;
        if (current == null) {
            throw new CommonException(CommonResultCodeEnum.INVALID_STATE);
        }
        current.trigger(task.getJobParams());
        return new ProcessResult(true);
    }
}
