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
package io.github.loadup.components.scheduler.powerjob;

import static io.github.loadup.components.scheduler.powerjob.PowerJobSchedulerProvider.PROCESSOR_NAME;

import io.github.loadup.components.scheduler.TaskSchedule;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import tech.powerjob.client.PowerJobClient;
import tech.powerjob.common.enums.ExecuteType;
import tech.powerjob.common.enums.ProcessorType;
import tech.powerjob.common.enums.SwitchableStatus;
import tech.powerjob.common.enums.TimeExpressionType;
import tech.powerjob.common.request.http.SaveJobInfoRequest;
import tech.powerjob.common.response.JobInfoDTO;
import tech.powerjob.common.response.ResultDTO;

final class PowerJobScheduleRegistry {
    static final String MANAGED_TAG = "loadup-scheduler";
    private final PowerJobClient client;

    PowerJobScheduleRegistry(PowerJobClient client) {
        this.client = client;
    }

    void synchronize(List<TaskSchedule> schedules) {
        Map<String, JobInfoDTO> managed = new LinkedHashMap<>();
        var desiredNames = schedules.stream().map(TaskSchedule::name).toList();
        var jobs = requireSuccess(client.fetchAllJob(), "list jobs");
        if (jobs == null) {
            throw new IllegalStateException("PowerJob did not return a job list");
        }
        for (var job : jobs) {
            if (Objects.equals(job.getStatus(), SwitchableStatus.DELETED.getV())) {
                continue;
            }
            if (MANAGED_TAG.equals(job.getTag())) {
                if (job.getId() == null) {
                    throw new IllegalStateException("PowerJob returned a managed definition without a job ID");
                }
                if (managed.putIfAbsent(job.getJobName(), job) != null) {
                    throw new IllegalStateException("Duplicate managed PowerJob jobs for '" + job.getJobName() + "'");
                }
            } else if (isEnabled(job)
                    && PROCESSOR_NAME.equals(job.getProcessorInfo())
                    && desiredNames.contains(job.getJobParams())) {
                throw new IllegalStateException("Disable the legacy PowerJob definition for '"
                        + job.getJobParams()
                        + "' before enabling its configured schedule");
            }
        }

        for (var schedule : schedules) {
            var existing = managed.remove(schedule.name());
            if (existing == null || !matches(existing, schedule)) {
                var request = new SaveJobInfoRequest();
                if (existing != null) {
                    request.setId(existing.getId());
                }
                request.setJobName(schedule.name());
                request.setJobParams(schedule.name());
                request.setTimeExpression(schedule.cron());
                request.setTimeExpressionType(TimeExpressionType.CRON);
                request.setExecuteType(ExecuteType.STANDALONE);
                request.setProcessorType(ProcessorType.BUILT_IN);
                request.setProcessorInfo(PROCESSOR_NAME);
                request.setMaxInstanceNum(1);
                request.setTag(MANAGED_TAG);
                Long jobId = requireSuccess(client.saveJob(request), "save '" + schedule.name() + "'");
                if (jobId == null) {
                    throw new IllegalStateException("PowerJob did not return a job ID for '" + schedule.name() + "'");
                }
            }
        }
        for (var job : managed.values()) {
            if (isEnabled(job)) {
                requireSuccess(client.disableJob(job.getId()), "disable '" + job.getJobName() + "'");
            }
        }
    }

    private static boolean matches(JobInfoDTO job, TaskSchedule schedule) {
        return isEnabled(job)
                && schedule.cron().equals(job.getTimeExpression())
                && Objects.equals(job.getTimeExpressionType(), TimeExpressionType.CRON.getV())
                && Objects.equals(job.getProcessorType(), ProcessorType.BUILT_IN.getV())
                && Objects.equals(job.getExecuteType(), ExecuteType.STANDALONE.getV())
                && PROCESSOR_NAME.equals(job.getProcessorInfo())
                && schedule.name().equals(job.getJobParams())
                && Objects.equals(job.getMaxInstanceNum(), 1)
                && Objects.equals(job.getInstanceRetryNum(), 0)
                && Objects.equals(job.getTaskRetryNum(), 0);
    }

    private static boolean isEnabled(JobInfoDTO job) {
        return Objects.equals(job.getStatus(), SwitchableStatus.ENABLE.getV());
    }

    private static <T> T requireSuccess(ResultDTO<T> result, String operation) {
        if (result == null || !result.isSuccess()) {
            throw new IllegalStateException("PowerJob could not " + operation);
        }
        return result.getData();
    }
}
