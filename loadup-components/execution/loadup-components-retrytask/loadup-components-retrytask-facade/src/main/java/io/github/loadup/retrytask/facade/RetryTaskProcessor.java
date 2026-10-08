package io.github.loadup.retrytask.facade;

import io.github.loadup.retrytask.facade.model.RetryTaskContext;

/**
 * SPI implemented by business code to process retry tasks of one business type.
 *
 * <p>Any exception thrown from {@link #process(RetryTaskContext)} marks the attempt as failed and
 * triggers the underlying retry engine. A successful return completes the task.
 */
public interface RetryTaskProcessor {

    /**
     * Returns the business type handled by this processor.
     *
     * @return the business type
     */
    String bizType();

    /**
     * Processes one retry task.
     *
     * @param context the task payload
     * @throws Exception when the attempt fails and should be retried
     */
    void process(RetryTaskContext context) throws Exception;
}
