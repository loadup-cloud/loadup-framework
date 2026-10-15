/*
 * #%L
 * Loadup Components Retrytask Test
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
package io.github.loadup.retrytask.test.jobrunr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import io.github.loadup.retrytask.jobrunr.JobRunrRetryTaskFacade;
import io.github.loadup.retrytask.jobrunr.RetryTaskJobRequest;
import io.github.loadup.retrytask.jobrunr.RetryTaskJobRequestHandler;
import io.github.loadup.retrytask.jobrunr.RetryTaskProperties;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.jobs.JobParameter;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.storage.JobNotFoundException;
import org.jobrunr.storage.StorageProvider;
import org.junit.jupiter.api.Test;

class RetryTaskJobRunrBoundaryTest {
    @Test
    void mutableJobPayloadSupportsJacksonConstructionAndDefensiveCopies() {
        var payload = new RetryTaskJobRequest();
        payload.setBizType("order");
        payload.setBizId("id");
        payload.setArgs(null);
        payload.setMaxRetries(2);
        assertThat(payload.getBizType()).isEqualTo("order");
        assertThat(payload.getBizId()).isEqualTo("id");
        assertThat(payload.getArgs()).isEmpty();
        assertThat(payload.getMaxRetries()).isEqualTo(2);
        Map<String, String> source = new java.util.HashMap<>(Map.of("key", "value"));
        payload.setArgs(source);
        source.clear();
        assertThat(payload.getArgs()).containsEntry("key", "value");
        assertThat(new RetryTaskJobRequest("order", "id", null, null).getArgs()).isEmpty();
        assertThat(payload.getJobRequestHandler()).isEqualTo(RetryTaskJobRequestHandler.class);
    }

    @Test
    void everyEngineStateMapsToItsPublicTaskStatus() {
        var storage = mock(StorageProvider.class);
        var job = mock(Job.class);
        when(storage.getJobById(any(UUID.class))).thenReturn(job);
        var facade = new JobRunrRetryTaskFacade(mock(JobRequestScheduler.class), storage, new RetryTaskProperties());
        Map<StateName, RetryTaskStatus> states = Map.of(
                StateName.AWAITING,
                RetryTaskStatus.PENDING,
                StateName.SCHEDULED,
                RetryTaskStatus.PENDING,
                StateName.ENQUEUED,
                RetryTaskStatus.PENDING,
                StateName.PROCESSING,
                RetryTaskStatus.PROCESSING,
                StateName.SUCCEEDED,
                RetryTaskStatus.SUCCEEDED,
                StateName.FAILED,
                RetryTaskStatus.FAILED,
                StateName.DELETED,
                RetryTaskStatus.DELETED);
        states.forEach((state, expected) -> {
            when(job.getState()).thenReturn(state);
            assertThat(facade.getStatus("order", "id")).contains(expected);
        });
    }

    @Test
    void missingAndLegacyPayloadsResetWithoutDiscardingGlobalRetryDefaults() {
        for (JobDetails details : new JobDetails[] {
            null,
            new JobDetails("legacy", null, "run", List.of()),
            new JobDetails("legacy", null, "run", List.of(new JobParameter("legacy")))
        }) {
            var storage = mock(StorageProvider.class);
            var scheduler = mock(JobRequestScheduler.class);
            var job = mock(Job.class);
            when(job.getState()).thenReturn(StateName.DELETED);
            when(job.getJobDetails()).thenReturn(details);
            when(storage.getJobById(any(UUID.class))).thenReturn(job);
            var facade = new JobRunrRetryTaskFacade(scheduler, storage, new RetryTaskProperties());
            facade.reset("order", "id");
            verify(scheduler).create(any(org.jobrunr.scheduling.JobBuilder.class));
            verify(storage, atLeastOnce()).deletePermanently(any(UUID.class));
        }
        var storage = mock(StorageProvider.class);
        when(storage.getJobById(any(UUID.class))).thenThrow(new JobNotFoundException(UUID.randomUUID()));
        var scheduler = mock(JobRequestScheduler.class);
        var facade = new JobRunrRetryTaskFacade(scheduler, storage, new RetryTaskProperties());
        facade.reset("order", "missing");
        verify(scheduler).create(any(org.jobrunr.scheduling.JobBuilder.class));
        assertThat(facade.getStatus("order", "missing")).isEmpty();
        facade.delete("order", "missing");
    }
}
