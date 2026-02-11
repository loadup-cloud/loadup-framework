package io.github.loadup.retrytask.infra.repository;

/*-
 * #%L
 * Loadup Components Retrytask Infrastructure
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

import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.model.RetryTask;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The repository for retry tasks.
 */
public interface RetryTaskRepository {

    /**
     * Saves a retry task.
     *
     * @param task The task to save.
     * @return The saved task.
     */
    RetryTask save(RetryTask task);

    /**
     * Finds a retry task by its business type and business identifier.
     *
     * @param bizType The business type of the task.
     * @param bizId The business identifier of the task.
     * @return An optional containing the task, or an empty optional if no task is found.
     */
    Optional<RetryTask> findByBizTypeAndBizId(String bizType, String bizId);

    /**
     * Deletes a retry task by its business type and business identifier.
     *
     * @param bizType The business type of the task.
     * @param bizId The business identifier of the task.
     */
    void delete(String bizType, String bizId);

    /**
     * Finds a batch of retry tasks that are ready to be executed.
     *
     * @param nextRetryTime The time to check for tasks to be retried.
     * @param limit The maximum number of tasks to find.
     * @return A list of retry tasks.
     */
    List<RetryTask> findTasksToRetry(LocalDateTime nextRetryTime, int limit);

    /**
     * Finds a retry task by its unique identifier.
     *
     * @param id The unique identifier of the task.
     * @return An optional containing the task, or an empty optional if no task is found.
     */
    Optional<RetryTask> findById(Long id);

    /**
     * Deletes a retry task by its unique identifier.
     *
     * @param id The unique identifier of the task.
     */
    void deleteById(Long id);

    /**
     * Attempts to lock a task for execution.
     * Sets status to RUNNING if status is PENDING.
     *
     * @param id The task ID.
     * @return true if locked successfully, false otherwise.
     */
    boolean tryLock(Long id);

    /**
     * Update task status.
     * @param id task id
     * @param status new status
     */
    void updateStatus(Long id, RetryTaskStatus status);

    /**
     * Reset tasks that are stuck in RUNNING status for too long.
     * @param deadTime deadline for determining stuck tasks
     * @return count of reset tasks
     */
    int resetStuckTasks(LocalDateTime deadTime);

    /**
     * Finds a batch of retry tasks for a specific business type that are ready to be executed.
     *
     * @param bizType The business type.
     * @param nextRetryTime The time to check for tasks to be retried.
     * @param limit The maximum number of tasks to find.
     * @return A list of retry tasks.
     */
    List<RetryTask> findTasksToRetryByBizType(String bizType, LocalDateTime nextRetryTime, int limit);
}
