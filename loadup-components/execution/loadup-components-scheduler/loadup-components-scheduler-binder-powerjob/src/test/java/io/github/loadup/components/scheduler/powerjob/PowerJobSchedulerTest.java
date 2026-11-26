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
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    void ignoresDisabledLegacyDefinitionsButRejectsUnsuccessfulPowerJobResponses() {
        var client = mock(PowerJobClient.class);
        var legacy = job(20L, "legacy", "0 * * * * ?");
        legacy.setTag(null);
        legacy.setStatus(SwitchableStatus.DISABLE.getV());
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of(legacy)));
        when(client.saveJob(any())).thenReturn(ResultDTO.success(21L));

        new PowerJobScheduleRegistry(client).synchronize(List.of(new TaskSchedule("scan", "0 * * * * *")));
        verify(client).saveJob(any());

        when(client.fetchAllJob()).thenReturn(ResultDTO.failed("synthetic rejection"));
        assertThatThrownBy(() -> new PowerJobScheduleRegistry(client).synchronize(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("list jobs");
    }

    @Test
    void rejectsUnsuccessfulDisableOfRemovedManagedJobs() {
        var client = mock(PowerJobClient.class);
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of(job(30L, "removed", "0 * * * * ?"))));
        when(client.disableJob(30L)).thenReturn(ResultDTO.failed("synthetic rejection"));

        assertThatThrownBy(() -> new PowerJobScheduleRegistry(client).synchronize(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disable 'removed'");
    }

    @Test
    void leavesMatchingManagedJobsAndDisabledOrDeletedDefinitionsAlone() {
        var client = mock(PowerJobClient.class);
        var matching = job(1L, "scan", "0 * * * * ?");
        var disabled = job(2L, "disabled", "0 * * * * ?");
        disabled.setStatus(SwitchableStatus.DISABLE.getV());
        var deleted = job(3L, "deleted", "0 * * * * ?");
        deleted.setStatus(SwitchableStatus.DELETED.getV());
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of(matching, disabled, deleted)));

        new PowerJobScheduleRegistry(client).synchronize(List.of(new TaskSchedule("scan", "0 * * * * *")));

        verify(client, never()).saveJob(any());
        verify(client, never()).disableJob(any());
    }

    static Stream<Arguments> changedManagedDefinitions() {
        return Stream.of(
                drift("disabled definition", job -> job.setStatus(SwitchableStatus.DISABLE.getV())),
                drift("missing trigger type", job -> job.setTimeExpressionType(null)),
                drift("missing processor type", job -> job.setProcessorType(null)),
                drift("missing execute type", job -> job.setExecuteType(null)),
                drift("different processor", job -> job.setProcessorInfo("otherProcessor")),
                drift("different task parameters", job -> job.setJobParams("another-task")),
                drift("concurrent instances", job -> job.setMaxInstanceNum(2)),
                drift("instance retry", job -> job.setInstanceRetryNum(1)),
                drift("task retry", job -> job.setTaskRetryNum(1)));
    }

    private static Arguments drift(String scenario, Consumer<JobInfoDTO> change) {
        return Arguments.of(scenario, change);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("changedManagedDefinitions")
    void reconcilesManagedDefinitionDriftInPlace(String scenario, Consumer<JobInfoDTO> change) {
        var client = mock(PowerJobClient.class);
        var existing = job(42L, "scan", "0 * * * * ?");
        change.accept(existing);
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of(existing)));
        when(client.saveJob(any())).thenReturn(ResultDTO.success(42L));

        new PowerJobScheduleRegistry(client).synchronize(List.of(new TaskSchedule("scan", "0 * * * * *")));

        var saved = ArgumentCaptor.forClass(SaveJobInfoRequest.class);
        verify(client).saveJob(saved.capture());
        var definition = saved.getValue();
        assertThat(definition.getId()).isEqualTo(42L);
        assertThat(definition.getProcessorInfo()).isEqualTo(PowerJobSchedulerProvider.PROCESSOR_NAME);
        assertThat(definition.getJobParams()).isEqualTo("scan");
        assertThat(definition.getMaxInstanceNum()).isEqualTo(1);
        verify(client, never()).disableJob(any());
    }

    @Test
    void disablesOnlyAnEnabledManagedJobAndRejectsMalformedRegistryResponses() {
        var client = mock(PowerJobClient.class);
        var stale = job(10L, "stale", "0 * * * * ?");
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of(stale)));
        when(client.disableJob(10L)).thenReturn(ResultDTO.success(null));

        new PowerJobScheduleRegistry(client).synchronize(List.of());

        verify(client).disableJob(10L);

        when(client.fetchAllJob()).thenReturn(null);
        assertThatThrownBy(() -> new PowerJobScheduleRegistry(client).synchronize(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("list jobs");
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(null));
        assertThatThrownBy(() -> new PowerJobScheduleRegistry(client).synchronize(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("job list");
    }

    @Test
    void rejectsDuplicateOrUnidentifiedManagedJobsAndFailedWrites() {
        var client = mock(PowerJobClient.class);
        var first = job(1L, "scan", "0 * * * * ?");
        var duplicate = job(2L, "scan", "0 * * * * ?");
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of(first, duplicate)));
        assertThatThrownBy(() -> new PowerJobScheduleRegistry(client).synchronize(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");

        duplicate.setTag(PowerJobScheduleRegistry.MANAGED_TAG);
        duplicate.setJobName("other");
        duplicate.setId(null);
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of(duplicate)));
        assertThatThrownBy(() -> new PowerJobScheduleRegistry(client).synchronize(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without a job ID");

        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of()));
        when(client.saveJob(any())).thenReturn(null);
        assertThatThrownBy(() -> new PowerJobScheduleRegistry(client)
                        .synchronize(List.of(new TaskSchedule("scan", "0 * * * * *"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("save");
        when(client.saveJob(any())).thenReturn(ResultDTO.success(null));
        assertThatThrownBy(() -> new PowerJobScheduleRegistry(client)
                        .synchronize(List.of(new TaskSchedule("scan", "0 * * * * *"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("job ID");
    }

    @Test
    void providerDispatchesNativeJobParametersAndPreservesTriggerFailures() throws Exception {
        var client = mock(PowerJobClient.class);
        when(client.fetchAllJob()).thenReturn(ResultDTO.success(List.of()));
        when(client.saveJob(any())).thenReturn(ResultDTO.success(12L));
        var provider = new PowerJobSchedulerProvider(client);
        var task = new TaskContext();
        task.setJobParams("business-task");

        var failure = new Exception("trigger failure");
        provider.start(List.of(new TaskSchedule("configured", "0 * * * * *")), name -> {
            assertThat(name).isEqualTo("business-task");
            throw failure;
        });

        assertThatThrownBy(() -> provider.process(task)).isSameAs(failure);
        provider.start(List.of(), name -> assertThat(name).isEqualTo("business-task"));
        assertThat(provider.process(task).isSuccess()).isTrue();
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

    @Test
    void filtersNativeAutoConfigurationForUnspecifiedBinderAndWorkerSettings() {
        var filter = new PowerJobAutoConfigurationFilter();
        var environment = new MockEnvironment();
        filter.setEnvironment(environment);
        var candidates = new String[] {
            "tech.powerjob.worker.autoconfigure.PowerJobAutoConfiguration",
            LoadUpPowerJobAutoConfiguration.class.getName(),
            SchedulerAutoConfiguration.class.getName()
        };

        assertThat(filter.match(candidates, null)).containsExactly(false, true, true);
        environment.setProperty("powerjob.client.enabled", "true");
        assertThat(filter.match(candidates, null)).containsExactly(false, true, true);
        environment.setProperty("powerjob.client.enabled", "false");
        assertThat(filter.match(candidates, null)).containsExactly(false, true, true);

        environment.setProperty("powerjob.server-address", "legacy-server:7700");
        assertThat(filter.match(candidates, null)).containsExactly(true, true, true);
        filter.setEnvironment(new MockEnvironment().withProperty("powerjob.worker.server-address", "server:7700"));
        assertThat(filter.match(candidates, null)).containsExactly(true, true, true);
        var worker = new MockEnvironment().withProperty("powerjob.worker.enabled", "TrUe");
        filter.setEnvironment(worker);
        assertThat(filter.match(candidates, null)).containsExactly(true, true, true);
        worker.setProperty("powerjob.worker.enabled", "false");
        assertThat(filter.match(candidates, null)).containsExactly(false, true, true);
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
