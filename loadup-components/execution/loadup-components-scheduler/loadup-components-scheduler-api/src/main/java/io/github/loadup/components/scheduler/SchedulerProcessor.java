package io.github.loadup.components.scheduler;

import io.github.loadup.components.scheduler.model.SchedulerContext;

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
