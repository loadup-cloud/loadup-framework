/*-
 * #%L
 * Loadup Components Retrytask Facade
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

package io.github.loadup.retrytask.facade;

import io.github.loadup.retrytask.facade.model.RetryTaskRequest;
import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import java.util.Optional;
import java.util.UUID;

/**
 * Business facade for the retry task component.
 *
 * <p>Registration is idempotent per {@code bizType + bizId}: while a job is still pending or
 * processing, registering the same pair is a no-op and returns the existing job id; once a job has
 * reached a terminal state (succeeded / failed / deleted), registering again replaces it with a
 * fresh job.
 */
public interface RetryTaskFacade {

    /**
     * Registers a retry task. Executes immediately unless {@link RetryTaskRequest#scheduleAt()} is
     * set.
     *
     * @param request the task to register
     * @return the id of the registered job
     */
    UUID register(RetryTaskRequest request);

    /**
     * Deletes the task identified by {@code bizType + bizId}. Deleting an unknown task is a no-op.
     *
     * @param bizType the business type
     * @param bizId the business identifier
     */
    void delete(String bizType, String bizId);

    /**
     * Deletes the task identified by {@code bizType + bizId} and re-enqueues it immediately with
     * its original payload. Unknown tasks are re-enqueued as fresh jobs.
     *
     * @param bizType the business type
     * @param bizId the business identifier
     */
    void reset(String bizType, String bizId);

    /**
     * Returns the current status of the task identified by {@code bizType + bizId}.
     *
     * @param bizType the business type
     * @param bizId the business identifier
     * @return the status, or empty when no job is known for the pair
     */
    Optional<RetryTaskStatus> getStatus(String bizType, String bizId);
}
