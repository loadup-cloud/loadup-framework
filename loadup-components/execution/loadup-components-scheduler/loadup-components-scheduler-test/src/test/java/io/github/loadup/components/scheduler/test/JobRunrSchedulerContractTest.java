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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.loadup.components.scheduler.jobrunr.JobRunrSchedulerTemplate;
import io.github.loadup.components.scheduler.jobrunr.SchedulerJobRequest;
import io.github.loadup.components.scheduler.model.ScheduleRequest;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.jobs.JobParameter;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.storage.RecurringJobsResult;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.navigation.OffsetBasedPageRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JobRunrSchedulerContractTest {
    @Test
    void missingRecurringJobsDoNotCreateOrDeleteAnything() {
        var scheduler = mock(JobRequestScheduler.class);
        var storage = mock(StorageProvider.class);
        when(storage.getRecurringJobs()).thenReturn(new RecurringJobsResult());
        var template = new JobRunrSchedulerTemplate(scheduler, storage);
        template.trigger("missing");
        template.delete("missing");
        template.updateCron("missing", "* * * * *");
        assertThat(template.getStatus("missing")).isEmpty();
        verifyNoInteractions(scheduler);
    }

    @Test
    void legacyPayloadsFallBackToEmptyArgumentsAndUnrelatedRecurringJobsAreIgnored() {
        for (JobDetails details : new JobDetails[] {
            null,
            new JobDetails("legacy", null, "run", List.of()),
            new JobDetails("legacy", null, "run", List.of(new JobParameter("legacy")))
        }) {
            var scheduler = mock(JobRequestScheduler.class);
            var storage = mock(StorageProvider.class);
            var other = mock(RecurringJob.class);
            when(other.getId()).thenReturn("other");
            when(other.getCreatedAt()).thenReturn(java.time.Instant.EPOCH);
            var recurring = mock(RecurringJob.class);
            when(recurring.getId()).thenReturn("task");
            when(recurring.getCreatedAt()).thenReturn(java.time.Instant.EPOCH);
            when(recurring.getJobDetails()).thenReturn(details);
            var recurringJobs = new RecurringJobsResult(List.of(other, recurring));
            when(storage.getRecurringJobs()).thenReturn(recurringJobs);
            new JobRunrSchedulerTemplate(scheduler, storage).trigger("task");
            var payload = ArgumentCaptor.forClass(SchedulerJobRequest.class);
            verify(scheduler).enqueue(payload.capture());
            assertThat(payload.getValue().getTaskName()).isEqualTo("task");
            assertThat(payload.getValue().getArgs()).isEmpty();
        }
    }

    @Test
    void registrationHandlesBlankZonesAndSkipsUnrelatedPendingJobs() {
        for (String zone : new String[] {null, " ", "Asia/Tokyo"}) {
            var scheduler = mock(JobRequestScheduler.class);
            var storage = mock(StorageProvider.class);
            var unrelated = mock(Job.class);
            when(unrelated.getRecurringJobId()).thenReturn(Optional.of("other"));
            var standalone = mock(Job.class);
            when(standalone.getRecurringJobId()).thenReturn(Optional.empty());
            when(storage.getJobs(any(), any(OffsetBasedPageRequest.class))).thenAnswer(invocation -> {
                OffsetBasedPageRequest page = invocation.getArgument(1);
                return page.mapToNewPage(2, List.of(unrelated, standalone));
            });
            new JobRunrSchedulerTemplate(scheduler, storage)
                    .register(new ScheduleRequest("task", "* * * * *", Map.of("id", "1"), zone));
            verify(scheduler)
                    .scheduleRecurrently(
                            eq("task"),
                            eq("* * * * *"),
                            eq(zone == null || zone.isBlank() ? ZoneId.systemDefault() : ZoneId.of(zone)),
                            any(SchedulerJobRequest.class));
            verify(storage, never()).deletePermanently(any(UUID.class));
        }
    }

    @Test
    void removingMoreThanOnePageDoesNotSkipShiftedPendingJobs() {
        var scheduler = mock(JobRequestScheduler.class);
        var storage = mock(StorageProvider.class);
        List<Job> pending = new ArrayList<>();
        for (int index = 0; index < 1001; index++) {
            var job = mock(Job.class);
            when(job.getId()).thenReturn(UUID.randomUUID());
            when(job.getRecurringJobId()).thenReturn(Optional.of("task"));
            pending.add(job);
        }
        when(storage.getJobs(any(), any(OffsetBasedPageRequest.class))).thenAnswer(invocation -> {
            OffsetBasedPageRequest page = invocation.getArgument(1);
            if (invocation.getArgument(0) != StateName.SCHEDULED) return page.emptyPage();
            int from = (int) Math.min(page.getOffset(), pending.size());
            return page.mapToNewPage(
                    pending.size(), new ArrayList<>(pending.subList(from, Math.min(from + 1000, pending.size()))));
        });
        when(storage.deletePermanently(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return pending.removeIf(job -> job.getId().equals(id)) ? 1 : 0;
        });
        new JobRunrSchedulerTemplate(scheduler, storage).register(ScheduleRequest.of("task", "* * * * *"));
        assertThat(pending).isEmpty();
        verify(storage, times(1001)).deletePermanently(any(UUID.class));
    }

    @Test
    void serializedJobPayloadSupportsDefaultConstructionAndDefensiveArgumentCopies() {
        var request = new SchedulerJobRequest();
        request.setTaskName("task");
        request.setArgs(null);
        assertThat(request.getTaskName()).isEqualTo("task");
        assertThat(request.getArgs()).isEmpty();
        Map<String, String> source = new java.util.LinkedHashMap<>(Map.of("id", "1"));
        request.setArgs(source);
        source.put("id", "changed");
        assertThat(request.getArgs()).containsEntry("id", "1");
        assertThat(new SchedulerJobRequest("task", null).getArgs()).isEmpty();
        assertThat(request.getJobRequestHandler())
                .isEqualTo(io.github.loadup.components.scheduler.jobrunr.SchedulerJobRequestHandler.class);
    }
}
