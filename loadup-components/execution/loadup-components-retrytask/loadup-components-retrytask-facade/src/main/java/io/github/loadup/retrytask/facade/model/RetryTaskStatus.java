package io.github.loadup.retrytask.facade.model;

/**
 * Binder-independent status of a retry task.
 */
public enum RetryTaskStatus {

    /** Waiting to run or being polled by the scheduler. */
    PENDING,

    /** Currently being executed. */
    PROCESSING,

    /** Completed successfully. */
    SUCCEEDED,

    /** Exhausted all retries or failed permanently. */
    FAILED,

    /** Explicitly deleted. */
    DELETED
}
