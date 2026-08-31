package io.github.loadup.components.scheduler;

import io.github.loadup.components.scheduler.model.SchedulerContext;

/**
 * SPI implemented by business code to process one recurring task.
 *
 * <p>Any exception thrown from {@link #process(SchedulerContext)} marks the run as failed and
 * triggers the underlying engine retry policy (same semantics as the retry task component).
 */
public interface SchedulerProcessor {

    /**
     * Returns the unique task name handled by this processor.
     *
     * @return the task name
     */
    String taskName();

    /**
     * Processes one scheduled run.
     *
     * @param context the task payload
     * @throws Exception when the run fails and should be retried by the engine
     */
    void process(SchedulerContext context) throws Exception;
}
