package io.github.loadup.components.scheduler.model;

public enum SchedulerStatus {
    /** The recurring task is registered and will fire according to its cron expression. */
    SCHEDULED,
    /** The recurring task is registered but temporarily suspended (only engines that support pausing). */
    PAUSED
}
