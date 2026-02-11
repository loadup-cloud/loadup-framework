package io.github.loadup.retrytask.infra.repository;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;

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

