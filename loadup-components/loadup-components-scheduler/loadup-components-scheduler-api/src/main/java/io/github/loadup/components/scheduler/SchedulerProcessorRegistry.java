package io.github.loadup.components.scheduler;

public interface SchedulerProcessorRegistry {

    /**
     * Returns the processor registered for the given task name.
     *
     * @param taskName the task name
     * @return the processor
     * @throws IllegalArgumentException when no processor is registered for the task name
     */
    SchedulerProcessor getProcessor(String taskName);
}
