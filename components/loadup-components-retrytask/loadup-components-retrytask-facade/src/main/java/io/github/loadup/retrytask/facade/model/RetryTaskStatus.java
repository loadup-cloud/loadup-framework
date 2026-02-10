package io.github.loadup.retrytask.facade.model;

/**
 * Represents the status of a retry task.
 */
public enum RetryTaskStatus {

    /**
     * The task is pending execution.
     */
    PENDING,

    /**
     * The task has been executed successfully.
     */
    SUCCESS,

    RUNNING,
    FAILED,
    MAX_RETRIES_EXCEEDED,
    /**
     * The task has failed.
     */
    FAILURE
}
