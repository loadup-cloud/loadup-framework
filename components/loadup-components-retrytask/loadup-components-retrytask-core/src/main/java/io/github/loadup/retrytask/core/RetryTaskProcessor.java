package io.github.loadup.retrytask.core;

import io.github.loadup.retrytask.facade.model.RetryTask;

/**
 * A processor for retry tasks.
 */
public interface RetryTaskProcessor {

    /**
     * Processes a retry task.
     *
     * @param task The task to process.
     * @return The result of the processing.
     */
    boolean process(RetryTask task);

    /**
     * Gets the business type of the tasks that this processor can handle.
     *
     * @return The business type.
     */
    String getBizType();
}
