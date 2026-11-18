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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.scheduler.ScheduledTask;
import io.github.loadup.components.scheduler.SchedulerAutoConfiguration;
import io.github.loadup.components.scheduler.SchedulerProvider;
import io.github.loadup.components.scheduler.SchedulerTemplate;
import io.github.loadup.components.scheduler.TaskSchedule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.env.MockEnvironment;
import tech.powerjob.client.PowerJobClient;
import tech.powerjob.common.enums.ExecuteType;
import tech.powerjob.common.enums.ProcessorType;
import tech.powerjob.common.enums.SwitchableStatus;
import tech.powerjob.common.enums.TimeExpressionType;
import tech.powerjob.common.request.http.SaveJobInfoRequest;
import tech.powerjob.common.response.JobInfoDTO;
import tech.powerjob.common.response.ResultDTO;
import tech.powerjob.worker.core.processor.TaskContext;

class PowerJobSchedulerTest {
    @Test
    void configuredJobIsCreatedAndDispatchedByTheNativeProcessor() {
        var client = mock(PowerJobClient.class);
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of()));
        when(client.saveJob(any())).thenReturn(ResultDTO.success(42L));
        var task = mock(ScheduledTask.class);
        when(task.name()).thenReturn("scan");
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        LoadUpPowerJobAutoConfiguration.class,
                        PowerJobSchedulerAutoConfiguration.class,
                        SchedulerAutoConfiguration.class))
                .withBean(PowerJobClient.class, () -> client)
                .withBean(ScheduledTask.class, () -> task)
                .withPropertyValues(
                        "loadup.scheduler.binder-type=powerjob",
                        "loadup.scheduler.tasks[0].name=scan",
                        "loadup.scheduler.tasks[0].cron=0 * * * * *")
                .run(context -> {
                    assertThat(context).hasSingleBean(SchedulerProvider.class).hasSingleBean(SchedulerTemplate.class);
                    var saved = ArgumentCaptor.forClass(SaveJobInfoRequest.class);
                    verify(client).saveJob(saved.capture());
                    assertThat(saved.getValue().getId()).isNull();
                    assertThat(saved.getValue().getJobName()).isEqualTo("scan");
                    assertThat(saved.getValue().getTimeExpression()).isEqualTo("0 * * * * ?");
                    assertThat(saved.getValue().getTimeExpressionType()).isEqualTo(TimeExpressionType.CRON);
                    assertThat(saved.getValue().getTag()).isEqualTo(PowerJobScheduleRegistry.MANAGED_TAG);

                    var job = new TaskContext();
                    job.setJobParams(saved.getValue().getJobParams());
                    var provider =
                            context.getBean(saved.getValue().getProcessorInfo(), PowerJobSchedulerProvider.class);
                    assertThat(provider.process(job).isSuccess()).isTrue();
                    verify(task).execute();
                    context.getBean(SchedulerTemplate.class).stop();
                    assertThatThrownBy(() -> provider.process(job)).isInstanceOf(CommonException.class);
                    verify(client, never()).disableJob(any());
                });
    }

    @Test
    void synchronizesByIdAndDisablesOnlyRemovedManagedJobs() {
        var client = mock(PowerJobClient.class);
        var same = job(1L, "same", "0 * * * * ?");
        var changed = job(2L, "changed", "0 5 * * * ?");
        var removed = job(3L, "removed", "0 * * * * ?");
        var nativeJob = job(4L, "native", "0 * * * * ?");
        nativeJob.setTag(null);
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of(same, changed, removed, nativeJob)));
        when(client.saveJob(any())).thenReturn(ResultDTO.success(2L));
        when(client.disableJob(3L)).thenReturn(ResultDTO.success(null));

        new PowerJobScheduleRegistry(client)
                .synchronize(
                        List.of(new TaskSchedule("same", "0 * * * * *"), new TaskSchedule("changed", "0 10 * * * *")));

        var saved = ArgumentCaptor.forClass(SaveJobInfoRequest.class);
        verify(client).saveJob(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(2L);
        assertThat(saved.getValue().getTimeExpression()).isEqualTo("0 10 * * * ?");
        verify(client).disableJob(3L);
        verify(client, never()).disableJob(4L);
    }

    @Test
    void refusesToCreateAnotherDefinitionWhenOneAlreadyDispatchesTheSameTask() {
        var client = mock(PowerJobClient.class);
        var job = job(1L, "scan", "0 * * * * ?");
        job.setTag(null);
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of(job)));
        var registry = new PowerJobScheduleRegistry(client);

        assertThatThrownBy(() -> registry.synchronize(List.of(new TaskSchedule("scan", "0 * * * * *"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legacy");
        verify(client, never()).saveJob(any());
    }

    @Test
    void selectingLocalSkipsTheNativeWorkerEvenWithPowerJobSettingsPresent() {
        var filter = new PowerJobAutoConfigurationFilter();
        var environment = new MockEnvironment().withProperty("powerjob.worker.server-address", "server:7700");
        filter.setEnvironment(environment);
        var candidates = new String[] {
            "tech.powerjob.worker.autoconfigure.PowerJobAutoConfiguration",
            LoadUpPowerJobAutoConfiguration.class.getName(),
            SchedulerAutoConfiguration.class.getName()
        };
        environment.setProperty("loadup.scheduler.binder-type", "local");
        assertThat(filter.match(candidates, null)).containsExactly(false, false, true);
        environment.setProperty("loadup.scheduler.binder-type", "powerjob");
        assertThat(filter.match(candidates, null)).containsExactly(true, true, true);
    }

    private static JobInfoDTO job(long id, String name, String cron) {
        var job = new JobInfoDTO();
        job.setId(id);
        job.setJobName(name);
        job.setJobParams(name);
        job.setTimeExpression(cron);
        job.setTag(PowerJobScheduleRegistry.MANAGED_TAG);
        job.setStatus(SwitchableStatus.ENABLE.getV());
        job.setTimeExpressionType(TimeExpressionType.CRON.getV());
        job.setExecuteType(ExecuteType.STANDALONE.getV());
        job.setProcessorType(ProcessorType.BUILT_IN.getV());
        job.setProcessorInfo(PowerJobSchedulerProvider.PROCESSOR_NAME);
        job.setMaxInstanceNum(1);
        job.setInstanceRetryNum(0);
        job.setTaskRetryNum(0);
        return job;
    }
}
