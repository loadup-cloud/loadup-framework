package io.github.loadup.retrytask.infra.api.management;

import io.github.loadup.retrytask.facade.model.RetryTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The repository for retry tasks.
 */
public interface RetryTaskManagement {

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
}
