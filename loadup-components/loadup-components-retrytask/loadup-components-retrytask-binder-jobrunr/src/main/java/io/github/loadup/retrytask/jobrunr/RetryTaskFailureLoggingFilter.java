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

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.ApplyStateFilter;
import org.jobrunr.jobs.states.FailedState;
import org.jobrunr.jobs.states.JobState;
import org.jobrunr.jobs.states.StateName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs a warning when a retry task exhausts its retries and permanently fails.
 *
 * <p>Integrators that need richer alerts (DingTalk, email, webhook) can replace this bean with
 * their own {@link ApplyStateFilter} implementation.
 */
public class RetryTaskFailureLoggingFilter implements ApplyStateFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryTaskFailureLoggingFilter.class);

    @Override
    public void onStateApplied(Job job, JobState oldState, JobState newState) {
        // The RetryFilter may elect FAILED and immediately reschedule a retry; only report when
        // FAILED is the final state of the job.
        if (newState instanceof FailedState failedState && job.getState() == StateName.FAILED) {
            LOGGER.warn(
                    "Retry task permanently failed: jobId={} name={} reason={}",
                    job.getId(),
                    job.getJobName(),
                    failedState.getMessage());
        }
    }
}
