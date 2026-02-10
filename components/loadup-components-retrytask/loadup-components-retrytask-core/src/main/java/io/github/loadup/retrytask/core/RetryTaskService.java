package io.github.loadup.retrytask.core;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;

import java.util.List;

/**
 * The service for managing retry tasks.
 */
public interface RetryTaskService {

    /**
     * Registers a new retry task.
     *
     * @param request The request to register a new retry task.
     * @return The unique identifier of the registered task.
     */
    Long register(RetryTaskRegisterRequest request);

    /**
     * Deletes a retry task.
     *
     * @param bizType The business type of the task.
     * @param bizId The business identifier of the task.
     */
    void delete(String bizType, String bizId);

    /**
     * Resets a retry task.
     *
     * @param bizType The business type of the task.
     * @param bizId The business identifier of the task.
     */
    void reset(String bizType, String bizId);

    /**
     * Pulls a batch of retry tasks that are ready to be executed.
     *
     * @param batchSize The maximum number of tasks to pull.
     * @return A list of retry tasks.
     */
    List<RetryTask> pullTasks(int batchSize);

    /**
     * Marks a task as successful.
     *
     * @param taskId The unique identifier of the task.
     */
    void markSuccess(Long taskId);

    /**
     * Marks a task as failed.
     *
     * @param taskId The unique identifier of the task.
     * @param reason The reason for the failure.
     */
    void markFailure(Long taskId, String reason);
}
