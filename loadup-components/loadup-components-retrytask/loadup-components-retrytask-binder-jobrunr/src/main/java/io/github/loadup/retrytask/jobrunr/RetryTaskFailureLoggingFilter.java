/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
