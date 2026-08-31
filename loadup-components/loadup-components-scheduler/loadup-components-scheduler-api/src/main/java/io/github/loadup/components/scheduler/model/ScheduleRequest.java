package io.github.loadup.components.scheduler.model;

import java.util.Map;
import java.util.Objects;

/**
 * Registration payload of a recurring task.
 *
 * @param taskName the unique task name, must match a registered {@code SchedulerProcessor}
 * @param cron the cron expression (or any engine-specific schedule expression) defining when to run
 * @param args optional string payload handed to the processor
 * @param zoneId optional timezone of the schedule; {@code null} falls back to the engine default
 */
public record ScheduleRequest(String taskName, String cron, Map<String, String> args, String zoneId) {

    public ScheduleRequest {
        Objects.requireNonNull(taskName, "taskName must not be null");
        Objects.requireNonNull(cron, "cron must not be null");
        args = args == null ? Map.of() : Map.copyOf(args);
    }

    /**
     * Creates a request with the system timezone.
     *
     * @param taskName the task name
     * @param cron the cron expression
     * @return the request
     */
    public static ScheduleRequest of(String taskName, String cron) {
        return new ScheduleRequest(taskName, cron, null, null);
    }

    /**
     * Creates a request with a payload and the system timezone.
     *
     * @param taskName the task name
     * @param cron the cron expression
     * @param args the payload
     * @return the request
     */
    public static ScheduleRequest of(String taskName, String cron, Map<String, String> args) {
        return new ScheduleRequest(taskName, cron, args, null);
    }
}
