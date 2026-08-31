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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jobrunr.jobs.states.StateName.AWAITING;
import static org.jobrunr.jobs.states.StateName.DELETED;
import static org.jobrunr.jobs.states.StateName.ENQUEUED;
import static org.jobrunr.jobs.states.StateName.FAILED;
import static org.jobrunr.jobs.states.StateName.PROCESSING;
import static org.jobrunr.jobs.states.StateName.SCHEDULED;
import static org.jobrunr.jobs.states.StateName.SUCCEEDED;

import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.model.RetryTaskRequest;
import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.jobs.JobParameter;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobBuilder;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.storage.JobNotFoundException;
import org.jobrunr.storage.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JobRunr backed {@link RetryTaskFacade}.
 *
 * <p>The job id is derived deterministically from {@code bizType + bizId} (SHA-256), which gives
 * registration its idempotency: JobRunr skips saving a job whose id already exists. Terminal jobs
 * (succeeded / failed / deleted) are replaced by a new registration.
 */
public class JobRunrRetryTaskFacade implements RetryTaskFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunrRetryTaskFacade.class);

    private final JobRequestScheduler scheduler;
    private final StorageProvider storageProvider;
    private final RetryTaskProperties properties;

    public JobRunrRetryTaskFacade(
            JobRequestScheduler scheduler, StorageProvider storageProvider, RetryTaskProperties properties) {
        this.scheduler = scheduler;
        this.storageProvider = storageProvider;
        this.properties = properties;
    }

    @Override
    public UUID register(RetryTaskRequest request) {
        UUID jobId = deterministicJobId(request.bizType(), request.bizId());
        replaceTerminalJob(jobId);

        RetryTaskJobRequest payload = new RetryTaskJobRequest(
                request.bizType(),
                request.bizId(),
                request.args(),
                properties.resolveMaxRetries(request.bizType(), request.maxRetries()));
        JobBuilder builder = JobBuilder.aJob()
                .withId(jobId)
                .withName(request.bizType() + ":" + request.bizId())
                .withJobRequest(payload);
        if (payload.getMaxRetries() != null) {
            builder.withAmountOfRetries(payload.getMaxRetries());
        }
        if (request.scheduleAt() != null) {
            builder.scheduleAt(request.scheduleAt());
        }
        scheduler.create(builder);
        LOGGER.debug(
                "Registered retry task bizType={} bizId={} jobId={} scheduledAt={}",
                request.bizType(),
                request.bizId(),
                jobId,
                request.scheduleAt());
        return jobId;
    }

    @Override
    public void delete(String bizType, String bizId) {
        UUID jobId = deterministicJobId(bizType, bizId);
        deleteInternal(jobId, "Deleted via RetryTaskFacade");
        LOGGER.debug("Deleted retry task bizType={} bizId={} jobId={}", bizType, bizId, jobId);
    }

    @Override
    public void reset(String bizType, String bizId) {
        UUID jobId = deterministicJobId(bizType, bizId);
        RetryTaskJobRequest payload = readPayload(jobId);
        deleteInternal(jobId, "Reset via RetryTaskFacade");
        register(new RetryTaskRequest(
                bizType,
                bizId,
                payload == null ? Map.of() : payload.getArgs(),
                null,
                payload == null ? null : payload.getMaxRetries()));
    }

    @Override
    public Optional<RetryTaskStatus> getStatus(String bizType, String bizId) {
        UUID jobId = deterministicJobId(bizType, bizId);
        try {
            return Optional.of(mapStatus(storageProvider.getJobById(jobId).getState()));
        } catch (JobNotFoundException e) {
            return Optional.empty();
        }
    }

    private void replaceTerminalJob(UUID jobId) {
        Job existing;
        try {
            existing = storageProvider.getJobById(jobId);
        } catch (JobNotFoundException e) {
            return;
        }
        if (isTerminal(existing.getState())) {
            storageProvider.deletePermanently(jobId);
        }
    }

    private void deleteInternal(UUID jobId, String reason) {
        try {
            Job job = storageProvider.getJobById(jobId);
            if (job.getState() == DELETED) {
                // JobRunr keeps deleted rows; physically remove them so a later register with the
                // same deterministic id can insert again.
                storageProvider.deletePermanently(jobId);
                return;
            }
            scheduler.delete(jobId, reason);
        } catch (JobNotFoundException e) {
            LOGGER.debug("No retry task to delete for jobId={}", jobId);
        }
    }

    private RetryTaskJobRequest readPayload(UUID jobId) {
        try {
            Job job = storageProvider.getJobById(jobId);
            JobDetails jobDetails = job.getJobDetails();
            if (jobDetails != null && !jobDetails.getJobParameters().isEmpty()) {
                JobParameter parameter = jobDetails.getJobParameters().get(0);
                if (parameter.getObject() instanceof RetryTaskJobRequest payload) {
                    return payload;
                }
            }
        } catch (JobNotFoundException e) {
            // fall through
        }
        return null;
    }

    private static boolean isTerminal(StateName state) {
        return state == SUCCEEDED || state == FAILED || state == DELETED;
    }

    private static RetryTaskStatus mapStatus(StateName state) {
        return switch (state) {
            case AWAITING, SCHEDULED, ENQUEUED -> RetryTaskStatus.PENDING;
            case PROCESSING -> RetryTaskStatus.PROCESSING;
            case SUCCEEDED -> RetryTaskStatus.SUCCEEDED;
            case FAILED -> RetryTaskStatus.FAILED;
            case DELETED -> RetryTaskStatus.DELETED;
        };
    }

    static UUID deterministicJobId(String bizType, String bizId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((bizType + ":" + bizId).getBytes(UTF_8));
            ByteBuffer buffer = ByteBuffer.wrap(hash);
            return new UUID(buffer.getLong(), buffer.getLong());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
