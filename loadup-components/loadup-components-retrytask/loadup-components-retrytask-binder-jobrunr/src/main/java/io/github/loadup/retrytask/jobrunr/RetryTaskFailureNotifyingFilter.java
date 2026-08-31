/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
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
package io.github.loadup.retrytask.jobrunr;

import io.github.loadup.retrytask.facade.RetryTaskNotifier;
import io.github.loadup.retrytask.facade.model.RetryTaskFailure;
import java.util.List;
import java.util.UUID;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.ApplyStateFilter;
import org.jobrunr.jobs.states.FailedState;
import org.jobrunr.jobs.states.JobState;
import org.jobrunr.jobs.states.StateName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JobRunr {@link ApplyStateFilter} that dispatches permanent failures to every registered {@link
 * RetryTaskNotifier}.
 *
 * <p>The {@code RetryFilter} may elect {@code FAILED} and immediately reschedule a retry; only the
 * final {@code FAILED} state of the job is reported.
 */
public class RetryTaskFailureNotifyingFilter implements ApplyStateFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryTaskFailureNotifyingFilter.class);

    private final List<RetryTaskNotifier> notifiers;

    public RetryTaskFailureNotifyingFilter(List<RetryTaskNotifier> notifiers) {
        this.notifiers = notifiers;
    }

    @Override
    public void onStateApplied(Job job, JobState oldState, JobState newState) {
        if (newState instanceof FailedState failedState && job.getState() == StateName.FAILED) {
            RetryTaskFailure failure = toFailure(job, failedState);
            LOGGER.debug("Dispatching permanent failure to {} notifier(s)", notifiers.size());
            for (RetryTaskNotifier notifier : notifiers) {
                try {
                    notifier.notifyFailed(failure);
                } catch (Exception e) {
                    LOGGER.warn(
                            "RetryTaskNotifier {} failed while reporting {}",
                            notifier.getClass().getName(),
                            failure,
                            e);
                }
            }
        }
    }

    private static RetryTaskFailure toFailure(Job job, FailedState failedState) {
        String jobName = job.getJobName();
        int separator = jobName.indexOf(':');
        String bizType = separator > 0 ? jobName.substring(0, separator) : jobName;
        String bizId = separator > 0 ? jobName.substring(separator + 1) : jobName;
        long failedStates = job.getJobStatesOfType(FailedState.class).count();
        return new RetryTaskFailure(
                bizType,
                bizId,
                job.getId() != null ? job.getId() : UUID.randomUUID(),
                (int) failedStates,
                failedState.getMessage());
    }
}
