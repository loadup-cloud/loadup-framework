/*
 * #%L
 * Loadup Components Scheduler Test
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
package io.github.loadup.components.scheduler.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.loadup.components.scheduler.SchedulerProcessor;
import io.github.loadup.components.scheduler.SchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.model.ScheduleRequest;
import io.github.loadup.components.scheduler.model.SchedulerStatus;
import io.github.loadup.components.scheduler.quartz.QuartzSchedulerTemplate;
import io.github.loadup.components.scheduler.quartz.SchedulerTaskJob;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.*;
import org.springframework.context.ApplicationContext;

class QuartzSchedulerContractTest {
    @Test
    void registerPreservesPayloadAndUsesDefaultOrExplicitTimeZone() throws Exception {
        for (String zone : new String[] {null, " ", "Asia/Tokyo"}) {
            var scheduler = mock(Scheduler.class);
            when(scheduler.checkExists(any(JobKey.class))).thenReturn(zone != null);
            new QuartzSchedulerTemplate(scheduler)
                    .register(new ScheduleRequest("task", "0 * * * * ?", Map.of("key", "value"), zone));
            var detail = ArgumentCaptor.forClass(JobDetail.class);
            var trigger = ArgumentCaptor.forClass(Trigger.class);
            verify(scheduler).scheduleJob(detail.capture(), trigger.capture());
            assertThat(detail.getValue().getJobDataMap().get("args")).isEqualTo(Map.of("key", "value"));
            assertThat(((CronTrigger) trigger.getValue()).getTimeZone().toZoneId())
                    .isEqualTo(zone == null || zone.isBlank() ? ZoneId.systemDefault() : ZoneId.of(zone));
            if (zone != null) verify(scheduler).deleteJob(JobKey.jobKey("task"));
        }
    }

    @Test
    void missingJobsAreNoOpsAndTriggerStatesMapToPublicStatus() throws Exception {
        var scheduler = mock(Scheduler.class);
        var template = new QuartzSchedulerTemplate(scheduler);
        template.trigger("missing");
        template.updateCron("missing", "0 * * * * ?");
        assertThat(template.getStatus("missing")).isEmpty();
        verify(scheduler, never()).triggerJob(any());
        verify(scheduler, never()).rescheduleJob(any(), any());
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);
        when(scheduler.getTriggerState(any())).thenReturn(Trigger.TriggerState.PAUSED, Trigger.TriggerState.NORMAL);
        assertThat(template.getStatus("task")).contains(SchedulerStatus.PAUSED);
        assertThat(template.getStatus("task")).contains(SchedulerStatus.SCHEDULED);
        template.trigger("task");
        verify(scheduler).triggerJob(JobKey.jobKey("task"));
        template.delete("task");
        verify(scheduler).deleteJob(JobKey.jobKey("task"));
    }

    @Test
    void cronUpdatesRetainOriginalZoneAndHandleMissingLegacyTrigger() throws Exception {
        var scheduler = mock(Scheduler.class);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);
        var previous = TriggerBuilder.newTrigger()
                .withIdentity("task")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 * * * * ?").inTimeZone(TimeZone.getTimeZone("Asia/Tokyo")))
                .build();
        when(scheduler.getTrigger(any())).thenReturn(previous, null);
        var template = new QuartzSchedulerTemplate(scheduler);
        template.updateCron("task", "0 */2 * * * ?");
        template.updateCron("legacy", "0 */2 * * * ?");
        var updated = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler, times(2)).rescheduleJob(any(), updated.capture());
        assertThat(((CronTrigger) updated.getAllValues().getFirst())
                        .getTimeZone()
                        .getID())
                .isEqualTo("Asia/Tokyo");
    }

    @Test
    void schedulerFailuresRetainOriginalCausesAcrossOperations() throws Exception {
        var scheduler = mock(Scheduler.class);
        var failure = new SchedulerException("synthetic scheduler outage");
        when(scheduler.checkExists(any(JobKey.class))).thenThrow(failure);
        when(scheduler.deleteJob(any())).thenThrow(failure);
        var template = new QuartzSchedulerTemplate(scheduler);
        List<Runnable> actions = List.of(
                () -> template.register(ScheduleRequest.of("task", "0 * * * * ?")),
                () -> template.trigger("task"),
                () -> template.delete("task"),
                () -> template.updateCron("task", "0 * * * * ?"),
                () -> template.getStatus("task"));
        for (var action : actions)
            assertThatThrownBy(action::run)
                    .isInstanceOf(IllegalStateException.class)
                    .hasCause(failure);
    }

    @Test
    void quartzJobAdaptsLegacyArgumentsAndWrapsProcessorFailure() throws Exception {
        var application = mock(ApplicationContext.class);
        var registry = mock(SchedulerProcessorRegistry.class);
        var processor = mock(SchedulerProcessor.class);
        when(application.getBean(SchedulerProcessorRegistry.class)).thenReturn(registry);
        when(registry.getProcessor("task")).thenReturn(processor);
        var context = mock(JobExecutionContext.class);
        var scheduler = mock(Scheduler.class);
        when(context.getScheduler()).thenReturn(scheduler);
        when(scheduler.getContext()).thenReturn(new org.quartz.SchedulerContext());
        var data = new JobDataMap();
        data.put("taskName", "task");
        when(context.getMergedJobDataMap()).thenReturn(data);
        var job = new SchedulerTaskJob();
        job.setApplicationContext(application);
        job.execute(context);
        var arguments = ArgumentCaptor.forClass(io.github.loadup.components.scheduler.model.SchedulerContext.class);
        verify(processor).process(arguments.capture());
        assertThat(arguments.getValue().args()).isEmpty();
        var failure = new IllegalStateException("synthetic processor failure");
        doThrow(failure).when(processor).process(any());
        assertThatThrownBy(() -> job.execute(context))
                .isInstanceOf(JobExecutionException.class)
                .hasCause(failure);
    }
}
