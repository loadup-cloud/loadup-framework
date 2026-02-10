package io.github.loadup.retrytask.facade;

import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;

/**
 * The facade for the retry task module.
 */
public interface RetryTaskFacade {

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
}
