package io.github.loadup.components.scheduler;

import io.github.loadup.components.scheduler.model.ScheduleRequest;
import io.github.loadup.components.scheduler.model.SchedulerStatus;
import java.util.Optional;

/**
 * Business facade for the scheduler component (recurring / cron tasks).
 *
 * <p>Registration is idempotent per {@code taskName}: registering an existing task updates its
 * cron expression and payload instead of creating a duplicate. One-shot or retried executions are
 * owned by the retry task component (JobRunr); this facade only manages recurring schedules.
 */
public interface SchedulerTemplate {

    /**
     * Registers or updates a recurring task. The underlying engine executes the registered
     * {@link SchedulerProcessor} according to the cron expression.
     *
     * @param request the schedule to register
     */
    void register(ScheduleRequest request);

    /**
     * Deletes the recurring task identified by {@code taskName}. Deleting an unknown task is a
     * no-op.
     *
     * @param taskName the task name
     */
    void delete(String taskName);

    /**
     * Triggers one immediate execution of the recurring task identified by {@code taskName},
     * without changing its schedule. Unknown tasks are a no-op.
     *
     * @param taskName the task name
     */
    void trigger(String taskName);

    /**
     * Updates the cron expression of an existing task. Unknown tasks are a no-op.
     *
     * @param taskName the task name
     * @param cron the new cron expression
     */
    void updateCron(String taskName, String cron);

    /**
     * Returns the current status of the recurring task identified by {@code taskName}.
     *
     * @param taskName the task name
     * @return the status, or empty when no task is registered under this name
     */
    Optional<SchedulerStatus> getStatus(String taskName);
}
